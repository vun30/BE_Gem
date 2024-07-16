package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.TypeBillEnum;
import online.gemfpt.BE.enums.TypeMoneyChange;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.exception.InsufficientMoneyInStallException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import online.gemfpt.BE.model.BuyBackProductRequest;
import online.gemfpt.BE.model.ProductUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


@Service
public class BuyBackService {
     @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private MetalService metalService;

    @Autowired
    private GemstoneRepository gemstoneRepository;

    @Autowired
    private MetalRepository metalRepository;

    @Autowired
    private ProductUrlRequest productUrlRequest;

     @Autowired
    private ProductUrlRepository productUrlRepository;

     @Autowired
     StallsSellRepository stallsSellRepository ;

     @Autowired
     AuthenticationService authenticationService ;

     @Autowired
     BillRepository billRepository;

     @Autowired
     BillItemRepository billItemRepository ;

     @Autowired
     BillBuyBackRepository buyBackRepository ;

     @Autowired
     BillBuyBackRepository billBuyBackRepository ;

     @Autowired
     CustomerRepository customerRepository ;

     @Autowired
     MoneyChangeHistoryRepository    moneyChangeHistoryRepository ;


public List<Bill> getAllBillOfCustomerForBuy(String customerPhone) {
        return billRepository.findByCustomerPhone(customerPhone);
    }


    @Transactional
    public BillBuyBack createBillAndProducts(String customerName, String customerPhone, List<BuyBackProductRequest> buyBackProductRequests) {
     // Kiểm tra xem khách hàng đã tồn tại hay chưa
    Optional<Customer> optionalCustomer = customerRepository.findByPhone(customerPhone);
    Customer customer;
    if (optionalCustomer.isPresent()) {
        customer = optionalCustomer.get();
    } else {
        // Nếu khách hàng chưa tồn tại, tạo mới
        customer = new Customer();
        customer.setName(customerName);
        customer.setPhone(customerPhone);
        customer.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        // Thiết lập các giá trị mặc định khác nếu cần
        customer.setPoints(0);
        customer.setRankCus("Normal");
        customer.setStatus(true);
        customer = customerRepository.save(customer);
    }
    // Create a new BillBuyBack
    BillBuyBack billBuyBack = new BillBuyBack();
    billBuyBack.setTypeBill(TypeBillEnum.BUY);
    billBuyBack.setCustomerName(customerName);
    billBuyBack.setCustomerPhone(customerPhone);
    billBuyBack.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    billBuyBack.setStatus(true); // Assuming the status is true initially
    Account account = authenticationService.getCurrentAccount();
    billBuyBack.setCashier(String.valueOf(account.getId()));
    billBuyBack.setStalls(account.getStallsWorkingId());

    // Save the BillBuyBack first
    BillBuyBack savedBillBuyBack = billBuyBackRepository.save(billBuyBack);

    // Create Products for the BillBuyBack and calculate prices
    for (BuyBackProductRequest buyBackProductRequest : buyBackProductRequests) {
        // Create a new Product
        Product product = new Product();
        product.setName(buyBackProductRequest.getName());
        product.setDescriptions(buyBackProductRequest.getDescriptions());
        product.setCategory(buyBackProductRequest.getCategory());
        product.setTypeWhenBuyBack(TypeOfProductEnum.PROCESSING);
     //   product.setPriceBuyRate(buyBackProductRequest.getPriceBuyRate());
        product.setStock(1);
        product.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        product.setStatus(false); // Assuming the status is false initially
        product.setBarcode(generateBarcode()); // Auto-generate barcode
        product.setBillBuyBack(savedBillBuyBack); // Set the saved BillBuyBack

        // Set URLs
        if (buyBackProductRequest.getUrls() != null) {
            List<ProductUrl> urls = buyBackProductRequest.getUrls().stream().map(productUrlRequest -> {
                ProductUrl url = new ProductUrl();
                url.setUrls(productUrlRequest.getUrls());
                url.setProduct(product);
                return url;
            }).collect(Collectors.toList());
            product.setUrls(urls);
        }

        // Create Gemstones
        if (buyBackProductRequest.getGemstones() != null) {
            List<Gemstone> gemstones = buyBackProductRequest.getGemstones().stream().map(gemstoneRequest -> {
                Gemstone gemstone = new Gemstone();
                gemstone.setDescription(gemstoneRequest.getDescription());
                gemstone.setColor(gemstoneRequest.getColor());
                gemstone.setClarity(gemstoneRequest.getClarity());
                gemstone.setCut(gemstoneRequest.getCut());
                gemstone.setCarat(gemstoneRequest.getCarat());
                gemstone.setPrice(gemstoneRequest.getPrice() - (gemstoneRequest.getPrice() * gemstoneRequest.getBuyRate() / 100));
                gemstone.setBuyRate(gemstoneRequest.getBuyRate());
                gemstone.setQuantity(gemstoneRequest.getQuantity());
                gemstone.setProduct(product);
                return gemstone;
            }).collect(Collectors.toList());
            product.setGemstones(gemstones);
        }

        // Create Metals
        if (buyBackProductRequest.getMetals() != null) {
            List<Metal> metals = buyBackProductRequest.getMetals().stream().map(metalRequest -> {
                Metal metal = new Metal();
                metal.setName(metalRequest.getName());
                metal.setDescription(metalRequest.getDescription());
                metal.setWeight(metalRequest.getWeight());
                metalService.setPricePerWeightUnitForBuyBack(metal);
                metal.setProduct(product);
                return metal;
            }).collect(Collectors.toList());
            product.setMetals(metals);
        }

        // Calculate total price of Metals and Gemstones
        double totalMetalPrice = 0;
        if (product.getMetals() != null) {
            for (Metal metal : product.getMetals()) {
                double metalPrice = metal.getPricePerWeightUnit();
                totalMetalPrice += metalPrice;
            }
        }

        double totalGemstonePrice = 0;
        if (product.getGemstones() != null) {
            totalGemstonePrice = product.getGemstones().stream()
                    .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                    .sum();
        }

        // Calculate final price of the Product
        double totalPrice1 = totalMetalPrice + totalGemstonePrice;
       // double totalPrice2 = totalPrice1 - (totalPrice1 * product.getPriceBuyRate() / 100);
        product.setPrice(totalPrice1); //totalPrice2

        // Add the product to the list of products in the bill
        savedBillBuyBack.getProducts().add(product);

        // Save the product with its components
        productsRepository.save(product);
        if (product.getGemstones() != null) {
            gemstoneRepository.saveAll(product.getGemstones());
        }
        if (product.getMetals() != null) {
            metalRepository.saveAll(product.getMetals());
        }
    }

       // Trừ số tiền từ total vào quầy của account đang login
    StallsSell stallsSell = stallsSellRepository.findById(account.getStallsWorkingId())
            .orElseThrow(() -> new StallsSellNotFoundException("Không tìm thấy quầy bán với ID: " + account.getStallsWorkingId()));
    double totalBillPrice = savedBillBuyBack.getProducts().stream()
            .mapToDouble(Product::getPrice)
            .sum();

     // Kiểm tra nếu số tiền trong quầy không đủ để trừ
    if (stallsSell.getMoney() < totalBillPrice) {
        throw new InsufficientMoneyInStallException("Số tiền trong quầy không đủ để thực hiện giao dịch này") ;
    }

    // Trừ số tiền và lưu lại vào quầy
    stallsSell.setMoney(stallsSell.getMoney() - totalBillPrice);
    stallsSellRepository.save(stallsSell);

      MoneyChangeHistory moneyChangeHistory = new MoneyChangeHistory();
    moneyChangeHistory.setStallsSell(stallsSell);
    moneyChangeHistory.setChangeDateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    moneyChangeHistory.setOldTotalInStall(stallsSell.getMoney() + totalBillPrice );
    moneyChangeHistory.setAmount(totalBillPrice); // Số tiền thay đổi là âm do làm giảm số tiền trong quầy
        moneyChangeHistory.setBillId(billBuyBack.getId());
          moneyChangeHistory.setStatus("Buy Back");
        moneyChangeHistory.setTypeChange(TypeMoneyChange.WITHDRAW);
    moneyChangeHistoryRepository.save(moneyChangeHistory);

    // Calculate total amount of the bill
    double totalAmount = savedBillBuyBack.getProducts().stream()
            .mapToDouble(Product::getPrice)
            .sum();
    savedBillBuyBack.setTotalAmount(totalAmount);

    // Save the updated BillBuyBack with products
    return billBuyBackRepository.save(savedBillBuyBack);
}
// Generate a random barcode not present in the database
private String generateBarcode() {
    String barcode;
    Optional<Product> existingProduct;

    do {
        barcode = generateRandomBarcode();
        existingProduct = productsRepository.findByBarcode(barcode);
    } while (existingProduct.isPresent());

    return barcode;
}

// Generate a random 8-digit numeric barcode
private String generateRandomBarcode() {
    int length = 8;
    Random random = new Random();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
        sb.append(random.nextInt(10));
    }

    return sb.toString();
}

public List<BillBuyBack> getAllBillBuyBacks() {
        return billBuyBackRepository.findAll();
    }
}

