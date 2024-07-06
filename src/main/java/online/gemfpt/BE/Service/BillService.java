package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.TypeBillEnum;
import online.gemfpt.BE.enums.TypeMoneyChange;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.exception.CustomerDiscountNotFoundException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import online.gemfpt.BE.model.BillRequest;
import online.gemfpt.BE.model.BillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    BillRepository billRepository;

    @Autowired
    BillItemRepository billItemRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    PromotionProductRepository promotionProductRepository;

    @Autowired
    WarrantyCardRepository warrantyCardRepository;

    @Autowired
    CustomerService customerService;

    @Autowired
    StallsSellRepository stallsSellRepository ;


    @Autowired
    MoneyChangeHistoryRepository moneyChangeHistoryRepository ;

    @Autowired
    DiscountRepository discountRepository ;


   public BillResponse addToCart(BillRequest billRequest) {
    String phone = billRequest.getCustomerPhone();
    List<String> barcodes = billRequest.getBarcodes();
    Long discountId = billRequest.getDiscountId();

    if (phone.isEmpty()) {
        throw new IllegalStateException("Phone number can not be empty!");
    }

    Optional<Customer> customer = customerRepository.findByPhone(phone);
    if (customer.isEmpty()) {
        throw new IllegalStateException("This phone number isn't signed up");
    }

    Account account = authenticationService.getCurrentAccount();

    if (!account.isStaffWorkingStatus()) {
        throw new IllegalStateException("Staff is not in working status.");
    }

    if (discountId != null && discountId != 00 && !customerRepository.existsByPhoneAndDiscounts_Id(phone, discountId)) {
        throw new CustomerDiscountNotFoundException(phone, discountId);
    }

    Bill bill = new Bill();
    bill.setTypeBill(TypeBillEnum.SEll);
    bill.setCustomerName(customer.get().getName());
    bill.setCustomerPhone(phone);
    bill.setCashier(account.getName());
    bill.setCreateTime(LocalDateTime.now());
    bill.setStalls(account.getStallsWorkingId());
    bill.setStatus(true);

    if (bill.getItems() == null) {
        bill.setItems(new ArrayList<>());
    }

    double totalAmount = 0;
    List<Product> products = productsRepository.findByBarcodeIn(barcodes);

    List<String> productExist = new ArrayList<>();
    for (String barcode : barcodes) {
        Optional<Product> product = productsRepository.findByBarcode(barcode);
        if (product.isPresent()) {
            if (product.get().getStock() <= 0) {
                throw new BadRequestException("Out of stock of: " + product.get().getBarcode());
            }
        } else {
            productExist.add(barcode);
        }
    }

    if (!productExist.isEmpty()) {
        throw new BadRequestException("Barcode " + productExist.toString() + " don't exist");
    }

    bill = billRepository.save(bill);

    List<WarrantyCard> warrantyCards = new ArrayList<>();
    for (Product product : products) {
        List<PromotionProduct> promotionProduct = promotionProductRepository.findByProductAndIsActive(product, true);
        double discount = 0;
        if (!promotionProduct.isEmpty()) {
            discount = promotionProduct.get(0).getDiscountValue();
        }

        double discountedPrice = product.getPrice() - (product.getPrice() * discount / 100);
        product.setNewPrice(discountedPrice);

        double totalPrice = product.getNewPrice() != null ? product.getNewPrice() : product.getPrice();

        int newQuantity = product.getStock() - 1;
        if (newQuantity < 0) {
            throw new BadRequestException("Not enough stock for product: " + product.getBarcode());
        }
        // Set stock to 0 after checkout
        BillItem billItem = new BillItem();
        billItem.setBill(bill);
        billItem.setProduct_barcode(product.getBarcode());
        billItem.setQuantity(1); // Giả định số lượng là 1 để đơn giản hóa
        billItem.setPrice(product.getPrice());
        billItem.setDiscount(discount);
        billItem.setNewPrice(totalPrice);
        product.setStock(0);
        totalAmount += totalPrice;
        bill.getItems().add(billItem);

        WarrantyCard warrantyCard = new WarrantyCard();
        warrantyCard.setCustomerName(customer.get().getName());
        warrantyCard.setCustomerPhone(customer.get().getPhone());
        warrantyCard.setProductBarcode(product.getBarcode());
        warrantyCard.setPurchaseDate(LocalDateTime.now());
        warrantyCard.setBill(bill);
        warrantyCard.setWarrantyExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.YEARS)); // Thời hạn bảo hành 1 năm
        warrantyCards.add(warrantyCard);

        warrantyCardRepository.save(warrantyCard);
        productsRepository.save(product);
    }

    double customer_point = totalAmount / 1000;
    customer.get().setPoints(customer.get().getPoints() + customer_point);

    double memberDiscount = 0;
    if (customer.get().getPoints() >= 5000000) {
        customer.get().setRankCus("Diamond");
        memberDiscount = 10;
    } else if (customer.get().getPoints() >= 1000000) {
        customer.get().setRankCus("Gold");
        memberDiscount = 8;
    } else if (customer.get().getPoints() >= 100000) {
        customer.get().setRankCus("Silver");
        memberDiscount = 5;
    } else {
        customer.get().setRankCus("Normal");
    }

    // Retrieve and apply the discount
    double discountRate = 0;
    if (discountId != null) {
        if (discountId != 00) {
            Discount discount = discountRepository.findById(discountId)
                    .orElseThrow(() -> new IllegalStateException("Discount not found with id: " + discountId));

            if (discount.isStatusUse()) {
                throw new BadRequestException("Discount with id " + discountId + " is already used.");
            }

            discountRate = discount.getRequestedDiscount();
            discount.setStatusUse(true);
            discountRepository.save(discount);
        }
    }

    double total = totalAmount - (totalAmount * memberDiscount / 100) - (totalAmount * discountRate / 100);
    bill.setVoucher(memberDiscount);
    bill.setDiscount(discountRate);

    customerRepository.save(customer.get());
    bill.setTotalAmount(total);
    bill = billRepository.save(bill);

    // Cộng tiền vào quầy và lưu lại
    StallsSell stallsSell = stallsSellRepository.findById(account.getStallsWorkingId())
            .orElseThrow(() -> new StallsSellNotFoundException("Không tìm thấy quầy bán với ID: " + account.getStallsWorkingId()));
    stallsSell.setMoney(stallsSell.getMoney() + total);
    stallsSellRepository.save(stallsSell);

    // Tạo và lưu lịch sử thay đổi tiền
    MoneyChangeHistory moneyChangeHistory = new MoneyChangeHistory();
    moneyChangeHistory.setStallsSell(stallsSell);
    moneyChangeHistory.setChangeDateTime(LocalDateTime.now());
    moneyChangeHistory.setAmount(total);
    moneyChangeHistory.setStatus("Sell ");
    moneyChangeHistory.setBillId(bill.getId());
    moneyChangeHistory.setTypeChange(TypeMoneyChange.ADD);
    moneyChangeHistoryRepository.save(moneyChangeHistory);

    BillResponse billResponse = new BillResponse();
    billResponse.setBill(bill);
    billResponse.getBill().setWarrantyCards(warrantyCards);

    return billResponse;
}


    public Bill getBillDetails(long id) {
        Optional<Bill> optionalBill = billRepository.findById(id);
        if (optionalBill.isPresent()) {
            return optionalBill.get();
        } else {
            throw new IllegalArgumentException("Invalid bill ID: " + id);
        }
    }

    public List<Bill> getAllBillOfCustomer(String customerPhone) {
        return billRepository.findByCustomerPhone(customerPhone);
    }

    public void deleteBill(long billId) {
        Optional<Bill> bill = billRepository.findById(billId);
        if (bill.isPresent()) {
            billRepository.deleteById(billId);
        } else {
            throw new IllegalArgumentException("Invalid bill ID :" + billId);
        }
    }



}
