package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.enums.TypeBillEnum;
import online.gemfpt.BE.enums.TypeMoneyChange;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.exception.InsufficientMoneyInStallException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import online.gemfpt.BE.model.BuyBackProductRequest;
import online.gemfpt.BE.model.GemstoneRequest;
import online.gemfpt.BE.model.ProductUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
     GemListRepository gemListRepository ;

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
        customer.setPoints(0);
        customer.setRankCus("Normal");
        customer.setStatus(true);
        customer = customerRepository.save(customer);
    }

    // Tạo hóa đơn mới
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

    // Prepare a map to store GemstoneRequest buyRates by gemBarcode
    Map<String, Double> buyRateMap = buyBackProductRequests.stream()
            .flatMap(request -> request.getGemstones().stream())
            .collect(Collectors.toMap(GemstoneRequest::getGemBarcode, GemstoneRequest::getBuyRate));

    // Get gem barcodes for lookup
    List<String> gemstoneBarcodes = new ArrayList<>(buyRateMap.keySet());

    // Get existing gemstones from the database
    List<Gemstone> existingGemstones = getGemstonesIfUse(gemstoneBarcodes);

    // Create Products for the BillBuyBack and calculate prices
    for (BuyBackProductRequest buyBackProductRequest : buyBackProductRequests) {
        // Create a new Product
        Product product = new Product();
        product.setName(buyBackProductRequest.getName());
        product.setDescriptions(buyBackProductRequest.getDescriptions());
        product.setCategory(buyBackProductRequest.getCategory());
        product.setTypeWhenBuyBack(TypeOfProductEnum.PROCESSING);
        product.setStock(1);
        product.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        product.setStatus(false); // Assuming the status is false initially
        product.setBarcode(generateRandomBarcode()); // Auto-generate barcode
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

        // Tạo viên đá quý
        if (buyBackProductRequest.getGemstones() != null) {
            List<Gemstone> gemstones = new ArrayList<>();
            for (GemstoneRequest gemstoneRequest : buyBackProductRequest.getGemstones()) {
                // Bỏ qua nếu gemstone barcode là null
                if (gemstoneRequest.getGemBarcode() == null) {
                    continue;
                }

                // Tìm viên đá quý trong GemList với trạng thái USE
                GemList gemList = gemListRepository.findByGemBarcode(gemstoneRequest.getGemBarcode())
                        .orElseThrow(() -> new BadRequestException("Gemstone with barcode " + gemstoneRequest.getGemBarcode() + " not found in GemList"));

                // Kiểm tra trạng thái của viên đá quý
                if (!GemStatus.USE.equals(gemList.getUserStatus())) {
                    throw new BadRequestException("Gemstone with barcode " + gemstoneRequest.getGemBarcode() + " is not in USE status");
                }

                // Tạo một viên đá quý mới với thông tin từ GemList
                Gemstone gemstone = new Gemstone();
                gemstone.setDescription(gemList.getDescription() + " | " + "Gem buy back in product barcode: " + product.getBarcode());
                gemstone.setColor(gemList.getColor());
                gemstone.setClarity(gemList.getClarity());
                gemstone.setCut(gemList.getCut());
                gemstone.setCarat(gemList.getCarat());
                gemstone.setPrice(gemList.getPrice() - (gemList.getPrice() * buyRateMap.get(gemstoneRequest.getGemBarcode()) / 100));
                gemstone.setBuyRate(buyRateMap.get(gemstoneRequest.getGemBarcode())); // Sử dụng tỷ lệ mua từ yêu cầu
                gemstone.setQuantity(1);
                gemstone.setUserStatus(GemStatus.PROCESSING);
                gemstone.setGemBarcode(generateRandomBarcode());
                gemstone.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                gemstone.setProduct(product);

                gemstones.add(gemstone);
            }
            product.setGemstones(gemstones);
        }

        // Create Metals
        if (buyBackProductRequest.getMetals() != null) {
            List<Metal> metals = buyBackProductRequest.getMetals().stream().map(metalRequest -> {
                Metal metal = new Metal();
                metal.setName(metalRequest.getName());
                metal.setDescription(metalRequest.getDescription());
                metal.setWeight(metalRequest.getWeight());
                metal.setDescription("Metal when buy back in product barcode :" + " " + product.getBarcode());
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
        throw new InsufficientMoneyInStallException("Số tiền trong quầy không đủ để thực hiện giao dịch này");
    }

    // Trừ số tiền và lưu lại vào quầy
    stallsSell.setMoney(stallsSell.getMoney() - totalBillPrice);
    stallsSellRepository.save(stallsSell);

    MoneyChangeHistory moneyChangeHistory = new MoneyChangeHistory();
    moneyChangeHistory.setStallsSell(stallsSell);
    moneyChangeHistory.setChangeDateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    moneyChangeHistory.setOldTotalInStall(stallsSell.getMoney() + totalBillPrice);
    moneyChangeHistory.setAmount(totalBillPrice); // Số tiền thay đổi là âm do làm giảm số tiền trong quầy
    moneyChangeHistory.setBillId(billBuyBack.getId());
    moneyChangeHistory.setStatus("Buy Back");
    moneyChangeHistory.setTypeChange(TypeMoneyChange.WITHDRAW);
    moneyChangeHistoryRepository.save(moneyChangeHistory);

    // Calculate total amount of the bill
    double totalAmount = savedBillBuyBack.getProducts().stream()
            .mapToDouble(Product::getPrice)
            .sum();

    // Set total amount on the bill
    savedBillBuyBack.setTotalAmount(totalAmount);

    // Save the bill again with updated total amount
    billBuyBackRepository.save(savedBillBuyBack);

    return savedBillBuyBack;
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

public Gemstone getGemstoneIfUse(String gemBarcode) {
    Optional<Gemstone> optionalGemstone = gemstoneRepository.findByGemBarcode(gemBarcode);
    if (optionalGemstone.isPresent()) {
        Gemstone gemstone = optionalGemstone.get();
        if (gemstone.getUserStatus() == GemStatus.USE) {
            return gemstone;
        } else {
            throw new BadRequestException("Gemstone with barcode " + gemBarcode + " does not have status USE.");
        }
    } else {
        throw new BadRequestException("Gemstone with barcode " + gemBarcode + " not found.") ;
    }
}

public List<Gemstone> getGemstonesIfUse(List<String> gemBarcodes) {
    List<Gemstone> gemstones = new ArrayList<>() ;
    for (String gemBarcode : gemBarcodes) {
        Gemstone gemstone = getGemstoneIfUse(gemBarcode);
        gemstones.add(gemstone);
    }
    return gemstones;
}

public List<BillBuyBack> getAllBillBuyBacks() {
        return billBuyBackRepository.findAll();
    }
}

