package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.TypeBillEnum;
import online.gemfpt.BE.exception.BadRequestException;
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
    DiscountRepository discountRepository;

    @Transactional
    public BillResponse addToCart(String name, int phone, List<String> barcodes, double discounts) {
        Optional<Customer> optionalCustomer = customerRepository.findByPhone(phone);
        Customer customer;
        if (optionalCustomer.isPresent()) {
            customer = optionalCustomer.get();
        } else {
            customer = new Customer();
            customer.setName(name);
            customer.setPhone(phone);
            customer.setCreateTime(LocalDateTime.now());
            customer = customerRepository.save(customer);
        }

        Account account = authenticationService.getCurrentAccount();

        if (!account.isStaffWorkingStatus()) {
            throw new IllegalStateException("Staff is not in working status.");
        }
        Bill bill = new Bill();
        bill.setTypeBill(TypeBillEnum.SEll);
        bill.setCustomerName(name);
        bill.setCustomerPhone(phone);
        bill.setCashier(account.getName());
        bill.setCreateTime(LocalDateTime.now());
        bill.setStalls(account.getStallsWorkingId());
        bill.setStatus(true);
        bill.setCreateTime(LocalDateTime.now());
        // Khởi tạo danh sách items nếu chưa được khởi tạo
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

            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setProduct_barcode(product.getBarcode());
            billItem.setQuantity(1); // Giả định số lượng là 1 để đơn giản hóa
            billItem.setPrice(product.getPrice());
            billItem.setDiscount(discount);
            billItem.setNewPrice(totalPrice);
            product.setStock(newQuantity);
            totalAmount += totalPrice;
            bill.getItems().add(billItem);

            WarrantyCard warrantyCard = new WarrantyCard();
            warrantyCard.setCustomerName(customer.getName());
            warrantyCard.setCustomerPhone(customer.getPhone());
            warrantyCard.setProductBarcode(product.getBarcode());
            warrantyCard.setPurchaseDate(LocalDateTime.now());
            warrantyCard.setBill(bill);
            warrantyCard.setWarrantyExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.YEARS)); // Thời hạn bảo hành 1 năm
            warrantyCards.add(warrantyCard);

            warrantyCardRepository.save(warrantyCard);
            productsRepository.save(product);
        }

        double customer_point = totalAmount / 1000;
        customer.setPoints(customer.getPoints() + customer_point);

        double memberDiscount = 0;
        if (customer.getPoints() >= 5000000) {
            customer.setRankCus("Diamond");
            memberDiscount = 10;
        } else if (customer.getPoints() >= 1000000) {
            customer.setRankCus("Gold");
            memberDiscount = 8;
        } else if (customer.getPoints() >= 100000) {
            customer.setRankCus("Silver");
            memberDiscount = 5;
        } else {
            customer.setRankCus("Normal");
        }

        double total = totalAmount - (totalAmount * memberDiscount / 100) - (totalAmount * discounts / 100);
        bill.setVoucher(memberDiscount);
        bill.setDiscount(discounts);

        customerRepository.save(customer);
        bill.setTotalAmount(total);
        bill = billRepository.save(bill);

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

    public List<Bill> getAllBillOfCustomer(int customerPhone) {
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

    public double payment(double amount, double customerCash) {
        if (customerCash >= amount) {
            return customerCash - amount;
        } else {
            throw new BadRequestException("Payment failed");
        }
    }

}
