package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.model.GemstoneRequest;
import online.gemfpt.BE.model.ProductUrlRequest;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@Service
public class ProductServices {
    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private online.gemfpt.BE.Service.MetalService metalService;

    @Autowired
    private GemstoneRepository gemstoneRepository;

    @Autowired
    private MetalRepository metalRepository;

    @Autowired
    private ProductUrlRequest productUrlRequest;

    @Autowired
    private ProductUrlRepository productUrlRepository;

    @Autowired
    UpdateProductHistoryRepository updateProductHistoryRepository;

    @Autowired
    PromotionProductRepository promotionProductRepository;

    @Autowired
    GemListRepository gemListRepository;

    @Autowired
    AuthenticationService authenticationService ;


    @Transactional
    public Product creates(ProductsRequest productsRequest) {
        // Kiểm tra xem sản phẩm có tồn tại không
        Optional<Product> existProduct = productsRepository.findByBarcodeAndStatus(productsRequest.getBarcode(), true);
        if (existProduct.isPresent()) {
            throw new BadRequestException("Barcode already exists!");
        }


        // Tạo mới một sản phẩm
        Product product = new Product();
        product.setName(productsRequest.getName());
        product.setDescriptions(productsRequest.getDescriptions());
        product.setCategory(productsRequest.getCategory());
        product.setPriceRate(productsRequest.getPriceRate());
        product.setStock(1);
        product.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        product.setStatus(true);
        product.setTypeWhenBuyBack(null);
        product.setBarcode(productsRequest.getBarcode());
        product.setWage(productsRequest.getWage());
        product.setStallId(productsRequest.getStallId());

        // Set URLs
        if (productsRequest.getUrls() != null) {
            List<ProductUrl> urls = productsRequest.getUrls().stream().map(productUrlRequest -> {
                ProductUrl url = new ProductUrl();
                url.setUrls(productUrlRequest.getUrls());
                url.setProduct(product);
                return url;
            }).collect(Collectors.toList());
            product.setUrls(urls);
        }


        // Tạo danh sách đá quý từ request và sử dụng findGemByBarCodes để lấy các thuộc tính

        if (productsRequest.getGemstones() != null) {
            List<String> gemstoneBarcodes = productsRequest.getGemstones().stream()
                    .map(GemstoneRequest::getGemBarcode)
                    .collect(Collectors.toList());

            for (String gemBarcode : gemstoneBarcodes) {
                if (checkGemstoneStatus(gemBarcode)) {
                    throw new BadRequestException("Gemstone with barcode " + gemBarcode + " has status different from NOTUSE");
                }
            }

            List<GemList> foundGemstones = findGemByBarCodes(gemstoneBarcodes);
            //   deleteListGem(gemstoneBarcodes);

            List<Gemstone> gemstones = foundGemstones.stream().map(gemstone -> {
                Gemstone newGemstone = new Gemstone();
                newGemstone.setGemBarcode(gemstone.getGemBarcode());
                newGemstone.setDescription(gemstone.getDescription());
                newGemstone.setColor(gemstone.getColor());
                newGemstone.setClarity(gemstone.getClarity());
                newGemstone.setCut(gemstone.getCut());
                newGemstone.setCarat(gemstone.getCarat());
                newGemstone.setPrice(gemstone.getPrice());
                newGemstone.setQuantity(gemstone.getQuantity());
                newGemstone.setUserStatus(GemStatus.USE);
                newGemstone.setCreateTime(gemstone.getCreateTime());
                newGemstone.setUrl(gemstone.getUrl());
                newGemstone.setProduct(product); // Set product for the new gemstone
                return newGemstone;
            }).collect(Collectors.toList());

            foundGemstones.forEach(gemList -> gemList.setUserStatus(GemStatus.USE));
            gemListRepository.saveAll(foundGemstones); // Lưu các GemList đã cập nhật

            product.setGemstones(gemstones); // Gán danh sách Gemstone cho sản phẩm
        }

        // Tạo danh sách kim loại từ request
        if (productsRequest.getMetals() != null) {
            List<Metal> metals = productsRequest.getMetals().stream().map(metalRequest -> {
                Metal metal = new Metal();
                metal.setName(metalRequest.getName());
                metal.setDescription(metalRequest.getDescription());
                metal.setWeight(metalRequest.getWeight());
                metal.setPriceMetal(metalService.setPricePerWeightUnit(metal));
                // Set price per weight unit
                metalService.setPricePerWeightUnit(metal); // Sử dụng service để set giá
                metal.setProduct(product);
                return metal;
            }).collect(Collectors.toList());
            product.setMetals(metals);
        }


        // Tính tổng giá của các kim loại
        double totalMetalPrice = 0;
        if (product.getMetals() != null) {
            for (Metal metal : product.getMetals()) {
                double metalPrice = metal.getPricePerWeightUnit();
                totalMetalPrice += metalPrice;
            }
        }

        // Tính tổng giá của các đá quý
        double totalGemstonePrice = 0;
        if (product.getGemstones() != null) {
            totalGemstonePrice = product.getGemstones().stream()
                    .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                    .sum();
        }

        double wege = productsRequest.getWage();

        // Tính giá cuối cùng của sản phẩm
        double totalPrice = wege + totalMetalPrice + totalGemstonePrice;
        double totalPrice2 = totalPrice + (totalPrice * product.getPriceRate() / 100);
        product.setPrice(totalPrice2);

        // Lưu sản phẩm và các thành phần của nó
        Product savedProduct = productsRepository.save(product);
        if (productsRequest.getGemstones() != null) {
            gemstoneRepository.saveAll(product.getGemstones());
        }
        if (productsRequest.getMetals() != null) {
            metalRepository.saveAll(product.getMetals());
        }
        return savedProduct;
    }

    public Product getProductByBarcode(String barcode) {
        // Lấy tài khoản đang đăng nhập
        Account account = authenticationService.getCurrentAccount();

        // Kiểm tra trạng thái làm việc của nhân viên
        if (!account.isStaffWorkingStatus()) {
            throw new BadRequestException("Staff is not currently working or status is invalid!");
        }

        // Extract the barcode part after the last '|'
        String[] parts = barcode.split("\\|");
        String lastPart = parts[parts.length - 1];

        // Tìm sản phẩm theo barcode, status và stallId
        Optional<Product> optionalProduct = productsRepository.findByBarcodeAndStatusAndStallId(lastPart, true, account.getStallsWorkingId());
        if (!optionalProduct.isPresent()) {
            throw new BadRequestException("Product not found with barcode: " + lastPart + " for the current stall.");
        }

        Product product = optionalProduct.get();

        // Kiểm tra và cập nhật khuyến mãi nếu có
        boolean hasActivePromotion = false;
        double newPrice = product.getPrice();
        List<Promotion> promotionList = new ArrayList<>();

        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            Promotion promotion = promotionProduct.getPromotion();
            if (promotion.isStatus()) {
                promotionList.add(promotion);
                hasActivePromotion = true;
            }
        }

        if (hasActivePromotion) {
            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        } else {
            product.setNewPrice(null);
        }

        return product;
    }


    public Product getProductByBarcodeMG(String barcode) {
        // Extract the barcode part after the last '|'
        String[] parts = barcode.split("\\|");
        String lastPart = parts[parts.length - 1];

        Optional<Product> optionalProduct = productsRepository.findByBarcodeAndStatus(lastPart, true);
        if (!optionalProduct.isPresent()) {
            throw new BadRequestException("Product not found with barcode: " + lastPart);
        }

        Product product = optionalProduct.get();
        boolean hasActivePromotion = false;
        double newPrice = product.getPrice();
        List<Promotion> promotionList = new ArrayList<>();

        // Check for active promotions
        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            Promotion promotion = promotionProduct.getPromotion();
            if (promotion.isStatus()) {
                promotionList.add(promotion);
                hasActivePromotion = true;
            }
        }

        // Apply promotions to calculate the new price
        if (hasActivePromotion) {
            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        } else {
            product.setNewPrice(null);
        }

        return product;
    }




    public Product toggleProductActive(String barcode) {
        Product product = productsRepository.findByBarcode(barcode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setStatus(!product.isStatus()); // Đảo ngược trạng thái hiện tại
        return productsRepository.save(product);
    }

    public List<Product> getAllProducts() {
        List<Product> productList = productsRepository.findAll();

        if (productList.isEmpty()) {
            throw new ProductNotFoundException("No products found!");
        }

        for (Product product : productList) {
            double newPrice = product.getPrice();
            boolean hasActivePromotion = false;

            for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
                Promotion promotion = promotionProduct.getPromotion();
                if (promotion.isStatus()) {
                    double discountRate = promotion.getDiscountRate() / 100;
                    newPrice -= (product.getPrice() * discountRate);
                    hasActivePromotion = true;
                }
            }

            if (!hasActivePromotion) {
                product.setNewPrice(null);
            } else {
                product.setNewPrice(newPrice);
            }
        }

        return productList;
    }

    @Transactional
    public Product updateProduct(String productBarcode, ProductsRequest productsRequest) {
        // Lấy thông tin sản phẩm hiện tại từ barcode
        Product existingProduct = productsRepository.findByBarcode(productBarcode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with barcode: " + productBarcode));

        UpdateProductHistory updateProductHistory = createUpdateProductHistory(existingProduct);
        updateProductHistoryRepository.save(updateProductHistory);
        // Xóa quan hệ với promotion
        List<String> gemBarcodes = existingProduct.getGemstones().stream()
                .map(Gemstone::getGemBarcode)
                .collect(Collectors.toList());

        // Xóa quan hệ với promotion
        deletePromotionProductsByBarcodes(existingProduct.getBarcode());

        // Xóa sản phẩm
        deleteProduct(existingProduct.getProductId());

        // Cập nhật trạng thái của tất cả gem trong sản phẩm bị xóa thành NOTUSE
        updateGemstonesStatusToNotUse(gemBarcodes);

        // Xóa promotion
        // (Giả sử có hàm xóa promotion cần thiết, nếu không có hãy thêm vào đây)

        // Tạo sản phẩm mới từ request
        return creates(productsRequest);
    }
////////////------------------------------------------------------------------------/////////////////////
    // bộ lọc tìm kiếm cho fe


    public List<Product> getAllProductsTrue() {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    Long stallsWorkingId = account.getStallsWorkingId();

    // Lấy danh sách sản phẩm có status là true
    List<Product> productList = productsRepository.findByStatus(true);

    if (productList.isEmpty()) {
        throw new BadRequestException("No products found!");
    }

    // Lọc danh sách sản phẩm dựa trên stallId và stallsWorkingId
    List<Product> filteredProductList = productList.stream()
            .filter(product -> product.getStallId() == stallsWorkingId)
            .collect(Collectors.toList());

    if (filteredProductList.isEmpty()) {
        throw new BadRequestException("No products found for the current stall!");
    }

    // Kiểm tra và áp dụng khuyến mãi cho các sản phẩm
    for (Product product : filteredProductList) {
        boolean hasActivePromotion = false;
        double newPrice = product.getPrice();
        List<Promotion> promotionList = new ArrayList<>();

        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            Promotion promotion = promotionProduct.getPromotion();
            if (promotion.isStatus()) {
                promotionList.add(promotion);
                hasActivePromotion = true;
            }
        }

        if (hasActivePromotion) {
            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        } else {
            product.setNewPrice(null);
        }
    }

    return filteredProductList;
}

public List<Product> getAllProductsTrueForMana() {
        List<Product> productList = productsRepository.findByStatus(true);

        if (productList.isEmpty()) {
            throw new ProductNotFoundException("No products found!");
        }

        for (Product product : productList) {
            boolean hasActivePromotion = false;
            double newPrice = product.getPrice();
            List<Promotion> promotionList = new ArrayList<>();

            for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
                Promotion promotion = promotionProduct.getPromotion();
                if (promotion.isStatus()) {
                    promotionList.add(promotion);
                    hasActivePromotion = true;
                }
            }

            if (hasActivePromotion) {
                for (Promotion promotion : promotionList) {
                    double discountRate = promotion.getDiscountRate() / 100;
                    newPrice = newPrice - (product.getPrice() * discountRate);
                }
                product.setNewPrice(newPrice);
            } else {
                product.setNewPrice(null);
            }
        }

        return productList;
    }



    public List<Product> searchProductsByGemstoneAttributes(String color, String clarity, String cut, Double carat) {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    List<Gemstone> gemstones = gemstoneRepository.findAll();

    List<Gemstone> filteredGemstones = gemstones.stream()
            .filter(gemstone -> (color == null || gemstone.getColor().equals(color)) &&
                    (clarity == null || gemstone.getClarity().equals(clarity)) &&
                    (cut == null || gemstone.getCut().equals(cut)) &&
                    (carat == null || gemstone.getCarat() == carat))
            .collect(Collectors.toList());

    List<Product> products = filteredGemstones.stream()
            .map(Gemstone::getProduct)
            .filter(product -> product.getStallId() == account.getStallsWorkingId() && product.isStatus())
            .distinct()
            .collect(Collectors.toList());

    // Tính và cập nhật newPrice cho mỗi sản phẩm trong danh sách đã lọc
    for (Product product : products) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return products;
}

public List<Product> searchProductsByMetalType(String metalType) {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    // Lấy tất cả các kim loại từ cơ sở dữ liệu
    List<Metal> metals = metalRepository.findAll();

    // Lọc các kim loại theo loại metalType
    List<Metal> filteredMetals = metals.stream()
            .filter(metal -> metal.getTypeOfMetal().getMetalType().equals(metalType))
            .collect(Collectors.toList());

    // Lọc các sản phẩm theo quầy làm việc của tài khoản và trạng thái true
    List<Product> products = filteredMetals.stream()
            .map(Metal::getProduct)
            .filter(product -> product.getStallId() == account.getStallsWorkingId() && product.isStatus())
            .distinct()
            .collect(Collectors.toList());

    // Tính và cập nhật newPrice cho mỗi sản phẩm trong danh sách đã lọc
    for (Product product : products) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return products;
}


    public List<Product> searchProductsByName(String name) {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    // Tìm sản phẩm theo tên và lọc theo quầy làm việc của tài khoản
    List<Product> products = productsRepository.findByNameContaining(name);
    List<Product> filteredProducts = products.stream()
            .filter(product -> product.getStallId() == account.getStallsWorkingId())
            .collect(Collectors.toList());

    // Tính và cập nhật newPrice cho mỗi sản phẩm trong danh sách đã lọc
    for (Product product : filteredProducts) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return filteredProducts;
}

public List<Product> searchProductsByNameStaff(String name) {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    // Tìm sản phẩm theo tên và lọc theo quầy làm việc của tài khoản
    List<Product> products = productsRepository.findByNameContaining(name);
    List<Product> filteredProducts = products.stream()
            .filter(product -> product.getStallId() == account.getStallsWorkingId() && product.isStatus())
            .collect(Collectors.toList());

    // Tính và cập nhật newPrice cho mỗi sản phẩm trong danh sách đã lọc
    for (Product product : filteredProducts) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return filteredProducts;
}


    public List<Product> getProductsByTypeWhenBuyBack(TypeOfProductEnum typeWhenBuyBack) {
        return productsRepository.findByTypeWhenBuyBack(typeWhenBuyBack);
    }

    public Product getProductById(Long productId) {
        Product product = productsRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        boolean hasActivePromotion = false;
        double newPrice = product.getPrice();
        List<Promotion> promotionList = new ArrayList<>();

        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            Promotion promotion = promotionProduct.getPromotion();
            if (promotion.isStatus()) {
                promotionList.add(promotion);
                hasActivePromotion = true;
            }
        }

        if (hasActivePromotion) {
            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        } else {
            product.setNewPrice(null);
        }

        return product;
    }


 public List<Product> getProductsByCategory(TypeEnum category) {
    // Lấy tài khoản đang đăng nhập
    Account account = authenticationService.getCurrentAccount();

    // Kiểm tra trạng thái làm việc của nhân viên
    if (!account.isStaffWorkingStatus()) {
        throw new BadRequestException("Staff is not currently working or status is invalid!");
    }

    // Tìm sản phẩm theo danh mục và lọc theo quầy làm việc của tài khoản
    List<Product> products = productsRepository.findByCategory(category);
    List<Product> filteredProducts = products.stream()
            .filter(product -> product.getStallId() == account.getStallsWorkingId() && product.isStatus())
            .collect(Collectors.toList());

    for (Product product : filteredProducts) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return filteredProducts;
}

    public List<GemList> findGemByBarCodes(List<String> barcodes) {
        List<GemList> gemLists = gemListRepository.findByBarcodes(barcodes);
        // Lấy danh sách các mã vạch từ kết quả tìm kiếm
        List<String> foundBarcodes = gemLists.stream()
                .map(GemList::getGemBarcode)
                .collect(Collectors.toList());

        // Xác định các mã vạch không tồn tại
        List<String> missingBarcodes = barcodes.stream()
                .filter(barcode -> !foundBarcodes.contains(barcode))
                .collect(Collectors.toList());

        // Ném lỗi nếu có mã vạch không tồn tại
        if (!missingBarcodes.isEmpty()) {
            throw new BadRequestException("The GemStone barcodes do not exist: " + String.join(", ", missingBarcodes));
        }


        return gemLists;
    }

    // Helper method to calculate the new price
    private double calculateNewPrice(Product product) {
        double newPrice = product.getPrice();
        List<Promotion> promotionList = new ArrayList<>();

        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            promotionList.add(promotionProduct.getPromotion());
        }

        for (Promotion promotion : promotionList) {
            double discountRate = promotion.getDiscountRate() / 100;
            newPrice = newPrice - (product.getPrice() * discountRate);
        }

        return newPrice;
    }

    public boolean checkGemstoneStatus(String gemBarcode) {  // UES IN GEM LIST
        return gemListRepository.existsByGemBarcodeAndUserStatusNot(gemBarcode, GemStatus.NOTUSE);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Optional<Product> productOpt = productsRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            productsRepository.delete(product);
        } else {
            throw new EntityNotFoundException("Product not found with id: " + productId);
        }
    }

    public void deletePromotionProductsByBarcodes(String barcodes) {
        promotionProductRepository.deleteByProductBarcode(barcodes);
    }

    public void updateGemstonesStatusToNotUse(List<String> gemBarcodes) {
        gemListRepository.updateGemStatusToNotUse(gemBarcodes, GemStatus.NOTUSE);
    }

    public UpdateProductHistory createUpdateProductHistory(Product product) {
        UpdateProductHistory updateProductHistory = new UpdateProductHistory();

        // Tạo barcode cho lịch sử
     //   String historyBarcode = generateUniqueBarcode(product.getBarcode()) + " - || Old barcode:" + " " + product.getBarcode();
        updateProductHistory.setBarcode(product.getBarcode());

        // Thiết lập thời gian tạo và cập nhật
        updateProductHistory.setCreateTime(product.getCreateTime());
        updateProductHistory.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        // Thiết lập mô tả lịch sử
        String metals = product.getMetals().stream()
                .map(metal -> String.format("Name: %s, Weight: %.2f, Price: %.2f, Description: %s",
                        metal.getName(),
                        metal.getWeight(),
                        metal.getPriceMetal(),
                        metal.getDescription()))
                .collect(Collectors.joining("; "));

        String gemstones = product.getGemstones().stream()
                .map(gemstone -> String.format("Barcode: %s, Description: %s, Price: %.2f, Quantity: %d, Color: %s, Clarity: %s, Cut: %s, Carat: %.2f",
                        gemstone.getGemBarcode(),
                        gemstone.getDescription(),
                        gemstone.getPrice(),
                        gemstone.getQuantity(),
                        gemstone.getColor(),
                        gemstone.getClarity(),
                        gemstone.getCut(),
                        gemstone.getCarat()))
                .collect(Collectors.joining("; "));

        String description = String.format("Update product: - Barcode: %s, Product Price : %2f, Name: %s, Category: %s, PriceRate: %.2f, Stock: %d, Wage: %.2f, Stall: %d, Metals: %s, Gemstones: %s, Type When Buy Back: %s, Create Time: %s, Update Time: %s",
                product.getBarcode(),
                product.getPrice(),
                product.getName(),
                product.getCategory(),
                product.getPriceRate(),
                product.getStock(),
                product.getWage(),
                product.getStallId(),
                metals,
                gemstones,
                product.getTypeWhenBuyBack(),
                product.getCreateTime(),
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        updateProductHistory.setDescriptions(description);
        return updateProductHistory;
    }

    //public Product updateAndCreateNewProductBuyBack(String barcode, ProductsRequest productsRequest) {
//    // Tìm kiếm sản phẩm theo barcode
//    Optional<Product> existingProductOptional = productsRepository.findByBarcodeAndStatus(barcode, false);
//    if (!existingProductOptional.isPresent()) {
//        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
//    }
//
//    Product existingProduct = existingProductOptional.get();
//
//    // Generate a new unique barcode for the existing product
//    String newUniqueBarcode = generateUniqueBarcode( barcode);
//
//    // Set the old product's barcode to the new unique barcode
//    existingProduct.setBarcode(newUniqueBarcode);
//    existingProduct.setTypeWhenBuyBack(TypeOfProductEnum.PROCESSINGDONE);
//    existingProduct.setStatus(false);
//    existingProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
//    productsRepository.save(existingProduct);
//
//    // Tạo sản phẩm mới với thông tin từ request
//    Product newProduct = new Product();
//    newProduct.setName(productsRequest.getName());
//    newProduct.setDescriptions(productsRequest.getDescriptions());
//    newProduct.setCategory(productsRequest.getCategory());
//    newProduct.setPriceRate(productsRequest.getPriceRate());
//    newProduct.setStock(1);
//    newProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
//    newProduct.setTypeWhenBuyBack(productsRequest.getTypeWhenBuyBack());
//    newProduct.setStatus(true);
//    newProduct.setOldID(String.valueOf(existingProduct.getProductId()));
//    newProduct.setBarcode(barcode); // Set barcode cho sản phẩm mới bằng barcode của sản phẩm cũ
//    newProduct.setWage(productsRequest.getWage());
//
//    // Set URLs
//    if (productsRequest.getUrls() != null) {
//        List<ProductUrl> urls = productsRequest.getUrls().stream().map(productUrlRequest -> {
//            ProductUrl url = new ProductUrl();
//            url.setUrls(productUrlRequest.getUrls());
//            url.setProduct(newProduct);
//            return url;
//        }).collect(Collectors.toList());
//        newProduct.setUrls(urls);
//    }
//
//
//
//    // Tạo danh sách đá quý từ request
//    if (productsRequest.getGemstones() != null) {
//        List<Gemstone> gemstones = productsRequest.getGemstones().stream().map(gemstoneRequest -> {
//            Gemstone gemstone = new Gemstone();
//            gemstone.setDescription(gemstoneRequest.getDescription());
//            gemstone.setColor(gemstoneRequest.getColor());
//            gemstone.setClarity(gemstoneRequest.getClarity());
//            gemstone.setCut(gemstoneRequest.getCut());
//            gemstone.setCarat(gemstoneRequest.getCarat());
//            gemstone.setPrice(gemstoneRequest.getPrice());
//            gemstone.setQuantity(gemstoneRequest.getQuantity());
//            gemstone.setGemBarcode(gemstoneRequest.getGemBarcode());
//            gemstone.setProduct(newProduct);
//            return gemstone;
//        }).collect(Collectors.toList());
//        newProduct.setGemstones(gemstones);
//    }
//    // Tạo danh sách kim loại từ request
//    if (productsRequest.getMetals() != null) {
//        List<Metal> metals = productsRequest.getMetals().stream().map(metalRequest -> {
//            Metal metal = new Metal();
//            metal.setName(metalRequest.getName());
//            metal.setDescription(metalRequest.getDescription());
//            metal.setWeight(metalRequest.getWeight());
//            metalService.setPricePerWeightUnit(metal);
//            metal.setProduct(newProduct);
//            return metal;
//        }).collect(Collectors.toList());
//        newProduct.setMetals(metals);
//    }
//
//    // Tính tổng giá của các kim loại
//    double totalMetalPrice = 0;
//    if (newProduct.getMetals() != null) {
//        for (Metal metal : newProduct.getMetals()) {
//            double metalPrice = metal.getPricePerWeightUnit();
//            totalMetalPrice += metalPrice;
//        }
//    }
//
//    // Tính tổng giá của các đá quý
//    double totalGemstonePrice = 0;
//    if (newProduct.getGemstones() != null) {
//        totalGemstonePrice = newProduct.getGemstones().stream()
//                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
//                .sum();
//    }
//    double wege = productsRequest.getWage();
//    // Tính giá cuối cùng của sản phẩm
//    double totalPrice = totalMetalPrice + totalGemstonePrice;
//    double totalPriceWithRate = wege + totalPrice + (totalPrice * newProduct.getPriceRate() / 100);
//    newProduct.setPrice(totalPriceWithRate);
//
//    // Lưu sản phẩm và các thành phần của nó
//    Product savedProduct = productsRepository.save(newProduct);
//    if (productsRequest.getGemstones() != null) {
//        gemstoneRepository.saveAll(newProduct.getGemstones());
//    }
//    if (productsRequest.getMetals() != null) {
//        metalRepository.saveAll(newProduct.getMetals());
//    }
//
//    return savedProduct;
//} // not use cause this function same function update product

    //    public List<Gemstone> findGemByBarCodesAndStatus(List<String> barcodes) {
//    List<Gemstone> gemstones = gemstoneRepository.findByBarcodes(barcodes);
//
//    // Kiểm tra xem danh sách gemstones có rỗng không
//    if (gemstones.isEmpty()) {
//        throw new EntityNotFoundException("No gemstones found with the provided barcodes.");
//    }
//
//    // Kiểm tra trạng thái của từng viên đá quý
//    for (Gemstone gemstone : gemstones) {
//        if (gemstone.getUserStatus() == GemStatus.USE) {
//            throw new BadRequestException("Gemstone with barcode " + gemstone.getGemBarcode() + " is already in use.");
//        }
//    }
//
//    // Cập nhật giá mới cho mỗi sản phẩm liên quan
//    for (Gemstone gemstone : gemstones) {
//        Product product = gemstone.getProduct();
//        if (product != null) {
//            double newPrice = calculateNewPrice(product);
//            product.setNewPrice(newPrice);
//        }
//    }
//
//    return gemstones;
//}

// @Transactional
//    public void updateGemstonesProductAndStatus(Long productId, List<String> gemBarcodes) {
//        gemstoneRepository.updateGemstones(productId, gemBarcodes, GemStatus.USE);
//    }
    //    private void updateProductPrice(Product product) {
//    // Tính tổng giá của các kim loại
//    double totalMetalPrice = 0;
//    if (product.getMetals() != null) {
//        for (Metal metal : product.getMetals()) {
//            double metalPrice = metal.getPricePerWeightUnit();
//            totalMetalPrice += metalPrice;
//        }
//    }
//
//    // Tính tổng giá của các đá quý
//    double totalGemstonePrice = 0;
//    if (product.getGemstones() != null) {
//        totalGemstonePrice = product.getGemstones().stream()
//                .filter(gemstone -> gemstone.getUserStatus() == GemStatus.USE) // Chỉ tính những đá quý đang được sử dụng
//                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
//                .sum();
//    }
//
//    double wage = product.getWage();
//
//    // Tính giá cuối cùng của sản phẩm
//    double totalPrice = totalMetalPrice + totalGemstonePrice;
//    double totalPriceWithRate = wage + totalPrice + (totalPrice * product.getPriceRate() / 100);
//    product.setPrice(totalPriceWithRate);
//    product.setStatus(false);
//
//    // Cập nhật sản phẩm trong cơ sở dữ liệu
//    productsRepository.save(product);
//}

//private void updateGemstoneStatus() {
//        List<Gemstone> gemstones = gemstoneRepository.findGemstonesWithNullProductAndUserStatusUse(GemStatus.USE);
//        for (Gemstone gemstone : gemstones) {
//            gemstone.setUserStatus(GemStatus.NOTUSE);
//        }
//        gemstoneRepository.saveAll(gemstones);
//    }

//    @Transactional
//public void unlinkGemsByProductBarcode(String barcode) {
//    // Tìm sản phẩm theo barcode
//    Optional<Product> productOptional = productsRepository.findByBarcodeAndStatus(barcode, true);
//    if (!productOptional.isPresent()) {
//        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
//    }
//
//    Product product = productOptional.get();
//    Long productId = product.getProductId();
//
//    deletePromotionProductsByBarcode(barcode);
//
//
//    // Tạo bản sao của sản phẩm
//    Product historyProduct = new Product();
//    historyProduct.setName(product.getName());
//    historyProduct.setDescriptions(product.getDescriptions());
//    historyProduct.setCategory(product.getCategory());
//    historyProduct.setPriceRate(product.getPriceRate());
//    historyProduct.setStock(product.getStock());
//    historyProduct.setTypeWhenBuyBack(product.getTypeWhenBuyBack());
//    historyProduct.setUpdateTime(product.getUpdateTime());
//    historyProduct.setStatus(false);
//    historyProduct.setBarcode(product.getBarcode());
//    historyProduct.setWage(product.getWage());
//    historyProduct.setOldID(String.valueOf(product.getProductId()));
//
//    // Tạo bản sao của URLs
//    if (product.getUrls() != null) {
//        List<ProductUrl> oldUrls = product.getUrls().stream().map(url -> {
//            ProductUrl oldUrl = new ProductUrl();
//            oldUrl.setUrls(url.getUrls());
//            oldUrl.setProduct(historyProduct);
//            return oldUrl;
//        }).collect(Collectors.toList());
//        historyProduct.setUrls(oldUrls);
//    }
//
//    // Tạo bản sao của Gemstones cũ và gán vào historyProduct
//    if (product.getGemstones() != null) {
//        List<Gemstone> oldGemstones = product.getGemstones().stream().map(gemstone -> {
//            Gemstone oldGemstone = new Gemstone();
//            oldGemstone.setGemBarcode(gemstone.getGemBarcode());
//            oldGemstone.setDescription(gemstone.getDescription());
//            oldGemstone.setColor(gemstone.getColor());
//            oldGemstone.setClarity(gemstone.getClarity());
//            oldGemstone.setCut(gemstone.getCut());
//            oldGemstone.setCarat(gemstone.getCarat());
//            oldGemstone.setPrice(gemstone.getPrice());
//            oldGemstone.setQuantity(gemstone.getQuantity());
//            oldGemstone.setUserStatus(GemStatus.FALSE); // Set trạng thái là FALSE
//            oldGemstone.setUrl(gemstone.getUrl());
//            oldGemstone.setUpdateTime(gemstone.getUpdateTime());
//            oldGemstone.setProduct(historyProduct); // Set product cho gemstone cũ
//            return oldGemstone;
//        }).collect(Collectors.toList());
//        historyProduct.setGemstones(oldGemstones);
//    }
//
//    // Set Metals cho sản phẩm cũ
//    if (product.getMetals() != null) {
//        List<Metal> oldMetals = product.getMetals().stream().map(metal -> {
//            Metal oldMetal = new Metal();
//            oldMetal.setName(metal.getName());
//            oldMetal.setDescription(metal.getDescription());
//            oldMetal.setWeight(metal.getWeight());
//            oldMetal.setPricePerWeightUnit(metal.getPricePerWeightUnit());
//            oldMetal.setProduct(historyProduct); // Set product cho metal cũ
//            return oldMetal;
//        }).collect(Collectors.toList());
//        historyProduct.setMetals(oldMetals);
//    }
//
//    // Tạo bản ghi lịch sử cho sản phẩm
//    UpdateProductHistory updateProductHistory = new UpdateProductHistory();
//    updateProductHistory.setBarcode(historyProduct.getBarcode());
//    updateProductHistory.setCreateTime(historyProduct.getCreateTime());
//    updateProductHistory.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
//    updateProductHistory.setDescriptions("Unlink gems and delete promotion from product: " +
//            "- Barcode: " + historyProduct.getBarcode() +
//            ", Name: " + historyProduct.getName() +
//            ", Category: " + historyProduct.getCategory() +
//            ", PriceRate: " + historyProduct.getPriceRate() +
//            ", Stock: " + historyProduct.getStock() +
//            ", Wage: " + historyProduct.getWage() +
//            ", Metals: " + historyProduct.getMetals().stream().map(Metal::getName).collect(Collectors.joining(", ")) +
//            ", TypeWhenBuyBack: " + historyProduct.getTypeWhenBuyBack()
//            + historyProduct.getTypeWhenBuyBack() + ", Create Time" + historyProduct.getCreateTime() + ", Update Time" + LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
//    );
//
//    // Lưu lịch sử sản phẩm vào cơ sở dữ liệu
//    updateProductHistoryRepository.save(updateProductHistory);
//
//    // Gỡ liên kết cho các gemstone có trạng thái là USE
//    gemstoneRepository.detachGemsByProductId(productId, GemStatus.USE, GemStatus.NOTUSE);
//}


//
//     public void validateGemBarcodes(List<String> gemBarcodes) {
//        if (gemBarcodes == null) {
//            // Nếu danh sách barcode là null, không thực hiện bất kỳ kiểm tra nào và kết thúc hàm
//            return;
//        }
//
//        // Loại bỏ các giá trị null và trống trong danh sách barcode
//        List<String> validBarcodes = gemBarcodes.stream()
//                .filter(barcode -> barcode != null && !barcode.trim().isEmpty())
//                .collect(Collectors.toList());
//
//        if (validBarcodes.isEmpty()) {
//            // Nếu sau khi lọc danh sách barcode không còn giá trị hợp lệ, không thực hiện kiểm tra và kết thúc hàm
//            return;
//        }
//
//        // Kiểm tra các barcode gem không hợp lệ hoặc đã sử dụng
//        List<Gemstone> invalidOrUsedGemstones = gemstoneRepository.findInvalidOrUsedGemstones(validBarcodes, GemStatus.NOTUSE);
//
//        if (!invalidOrUsedGemstones.isEmpty()) {
//            String invalidBarcodes = invalidOrUsedGemstones.stream()
//                    .map(Gemstone::getGemBarcode)
//                    .collect(Collectors.joining(", "));
//            throw new IllegalArgumentException("Invalid or used gemstones with barcodes: " + invalidBarcodes);
//        }
//    }

//
//public void detachGemstonesByProductBarcode(Long id) {
//    Product product = productsRepository.findById(id)
//            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
//
//    Long productId = product.getProductId();
//    detachGemstonesByProductId(productId);
//    updateGemstoneStatus();
//    updateProductPrice(product); // Tính lại giá sau khi tháo đá quý
//}

//  public List<Product> findProductsInPriceRangeAndStatus(double minPrice, double maxPrice) {
//        return productsRepository.findByPriceBetweenAndStatus(minPrice, maxPrice,true);
//    }

    //@Transactional
//public void deleteListGem(List<String> barcodes) {
//    // Tìm kiếm các Gemstone bằng barcode
//    List<Gemstone> gemstonesToDelete = gemstoneRepository.findByBarcodes(barcodes);
//
//    // Xóa các Gemstone đã tìm thấy
//    gemstoneRepository.deleteAll(gemstonesToDelete);
//}


//    private void detachGemstonesByProductId(Long productId) {
//        gemstoneRepository.detachFromProductById(productId, GemStatus.NOTUSE);
//    }

    // hàm tà đạo
    private String generateUniqueBarcode(String existingBarcode) {
        // Example: Add a prefix "UP:" followed by a unique string to mark an update
        String prefix = "Don't care this code :";
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniqueString;
    }

    public void deletePromotionProductsByBarcode(String barcode) {
        if (barcode != null && !barcode.trim().isEmpty()) {
            promotionProductRepository.deleteByProductBarcode(barcode);
        }
    }

    public Product findProductByBarcode(String barcode) {
        // Find the product by barcode or throw an exception if not found
        Product product = productsRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BadRequestException("Product not found with barcode: " + barcode));

        // Initialize the new price as the original price
        double newPrice = product.getPrice();

        // Check for active promotions and update the new price if any
        for (PromotionProduct promotionProduct : product.getPromotionProducts()) {
            Promotion promotion = promotionProduct.getPromotion();
            if (promotion.isStatus()) { // Only apply promotions that are active (status = true)
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice -= product.getPrice() * discountRate;
            }
        }

        // Update the new price if it has been changed by active promotions
        if (newPrice != product.getPrice()) {
            product.setNewPrice(newPrice);
        } else {
            product.setNewPrice(null); // If no promotion is active, set new price to null
        }

        return product;
    }


    public List<UpdateProductHistory> getProductUpdateHistoryByBarcode(String barcode) {
        List<UpdateProductHistory> historyList = updateProductHistoryRepository.findByBarcodeOrderByCreateTimeAsc(barcode);
        if (historyList.isEmpty()) {
            throw new BadRequestException("No update history found for product with barcode: " + barcode);
        }
        return historyList;
    }

public List<Product> getProductsByStallId(Long stallId) {
    List<Product> products = productsRepository.findByStallId(stallId);

    if (products.isEmpty()) {
        throw new BadRequestException("No products found for stall with ID: " + stallId);
    }

    // Tính và cập nhật newPrice cho mỗi sản phẩm trong danh sách đã lọc
    for (Product product : products) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return products;
}

}
