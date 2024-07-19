package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.model.GemstoneRequest;
import online.gemfpt.BE.model.ProductUrlRequest;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
     UpdateProductHistoryRepository updateProductHistoryRepository ;

     @Autowired
     PromotionProductRepository promotionProductRepository ;



public Product creates(ProductsRequest productsRequest) {
    // Kiểm tra xem sản phẩm có tồn tại không
    Optional<Product> existProduct = productsRepository.findByBarcodeAndStatus(productsRequest.getBarcode(),true);
    if (existProduct.isPresent()) {
        throw new IllegalArgumentException("Barcode already exists!");
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
    product.setBarcode(productsRequest.getBarcode());
    product.setWage(productsRequest.getWage());

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
                    throw new IllegalArgumentException("Gemstone with barcode " + gemBarcode + " has status different from NOTUSE");
                }
            }

    List<Gemstone> foundGemstones = findGemByBarCodes(gemstoneBarcodes);
    deleteListGem(gemstoneBarcodes);

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

    product.setGemstones(gemstones);
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
            double metalPrice =  metal.getPricePerWeightUnit();
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
    double totalPrice2 = totalPrice  + (totalPrice * product.getPriceRate() / 100);
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
        // Extract the barcode part after the last '|'
        String[] parts = barcode.split("\\|");
        String lastPart = parts[parts.length - 1];

        Optional<Product> optionalProduct = productsRepository.findByBarcodeAndStatus(lastPart, true);
        if (!optionalProduct.isPresent()) {
            throw new ProductNotFoundException("Product not found with barcode: " + lastPart);
        }
        return optionalProduct.get();
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
        for (Product product : productList){
            double newPrice = product.getPrice();
            List<Promotion> promotionList = new ArrayList<>();
            for (PromotionProduct promotionProduct : product.getPromotionProducts()){
                promotionList.add(promotionProduct.getPromotion());
            }

            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        }
        return productList;
    }

public Product updateAndCreateNewProduct(String barcode, ProductsRequest productsRequest) {

    // Tìm kiếm sản phẩm theo barcode
    Optional<Product> existingProductOptional = productsRepository.findByBarcodeAndStatus(barcode, true);
    if (!existingProductOptional.isPresent()) {
        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
    }

    Product existingProduct = existingProductOptional.get();

    // Tạo bản sao của sản phẩm hiện tại như lịch sử sản phẩm
    Product historyProduct = new Product();
    historyProduct.setName(existingProduct.getName());
    historyProduct.setDescriptions(existingProduct.getDescriptions());
    historyProduct.setCategory(existingProduct.getCategory());
    historyProduct.setPriceRate(existingProduct.getPriceRate());
    historyProduct.setStock(existingProduct.getStock());
    historyProduct.setTypeWhenBuyBack(existingProduct.getTypeWhenBuyBack());
    historyProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    historyProduct.setStatus(false);
    historyProduct.setBarcode(existingProduct.getBarcode());
    historyProduct.setWage(existingProduct.getWage());
    historyProduct.setOldID(String.valueOf(existingProduct.getProductId()));

    // Tạo bản sao của URLs
    if (existingProduct.getUrls() != null) {
        List<ProductUrl> oldUrls = existingProduct.getUrls().stream().map(url -> {
            ProductUrl oldUrl = new ProductUrl();
            oldUrl.setUrls(url.getUrls());
            oldUrl.setProduct(historyProduct);
            return oldUrl;
        }).collect(Collectors.toList());
        historyProduct.setUrls(oldUrls);
    }

    // Tạo bản sao của Gemstones cũ và gán vào historyProduct
    if (existingProduct.getGemstones() != null) {
        List<Gemstone> oldGemstones = existingProduct.getGemstones().stream().map(gemstone -> {
            Gemstone oldGemstone = new Gemstone();
            oldGemstone.setGemBarcode(gemstone.getGemBarcode() );
            oldGemstone.setDescription(gemstone.getDescription());
            oldGemstone.setColor(gemstone.getColor());
            oldGemstone.setClarity(gemstone.getClarity());
            oldGemstone.setCut(gemstone.getCut());
            oldGemstone.setCarat(gemstone.getCarat());
            oldGemstone.setPrice(gemstone.getPrice());
            oldGemstone.setQuantity(gemstone.getQuantity());
            oldGemstone.setUserStatus(GemStatus.FALSE); // Set trạng thái là FALSE
            oldGemstone.setUrl(gemstone.getUrl());
            oldGemstone.setUpdateTime(gemstone.getUpdateTime());
            oldGemstone.setProduct(historyProduct); // Set product cho gemstone cũ
            return oldGemstone;
        }).collect(Collectors.toList());
        historyProduct.setGemstones(oldGemstones);
    }

    // Set Metals cho sản phẩm cũ
    if (existingProduct.getMetals() != null) {
        List<Metal> oldMetals = existingProduct.getMetals().stream().map(metal -> {
            Metal oldMetal = new Metal();
            oldMetal.setName(metal.getName());
            oldMetal.setDescription(metal.getDescription());
            oldMetal.setWeight(metal.getWeight());
            oldMetal.setPricePerWeightUnit(metal.getPricePerWeightUnit());
            oldMetal.setProduct(historyProduct); // Set product cho metal cũ
            return oldMetal;
        }).collect(Collectors.toList());
        historyProduct.setMetals(oldMetals);
    }

    // Lưu lịch sử sản phẩm vào cơ sở dữ liệu
  //  productsRepository.save(historyProduct);

    // Xóa sản phẩm hiện tại
    productsRepository.delete(existingProduct);

    // Tạo sản phẩm mới với mã vạch và thông tin từ request
    Product newProduct = new Product();
    newProduct.setName(productsRequest.getName());
    newProduct.setDescriptions(productsRequest.getDescriptions());
    newProduct.setCategory(productsRequest.getCategory());
    newProduct.setPriceRate(productsRequest.getPriceRate());
    newProduct.setStock(1);
    newProduct.setTypeWhenBuyBack(null);
    newProduct.setCreateTime(existingProduct.getCreateTime());
    newProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    newProduct.setStatus(true);
    newProduct.setBarcode(barcode);
    newProduct.setWage(productsRequest.getWage());

     // Xử lý danh sách URLs mới
    if (productsRequest.getUrls() != null) {
        List<ProductUrl> urls = productsRequest.getUrls().stream().map(urlRequest -> {
            ProductUrl url = new ProductUrl();
            url.setUrls(urlRequest.getUrls());
            url.setProduct(newProduct);
            return url;
        }).collect(Collectors.toList());
        newProduct.setUrls(urls);
    }

    // Xử lý danh sách gemstones mới
    List<String> gemstoneBarcodes = productsRequest.getGemstones().stream()
            .map(GemstoneRequest::getGemBarcode)
            .collect(Collectors.toList());

    List<Gemstone> foundGemstones = findGemByBarCodes(gemstoneBarcodes);
    deleteListGem(gemstoneBarcodes);

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
        newGemstone.setUserStatus(GemStatus.USE); // Set trạng thái là USE
        newGemstone.setCreateTime(gemstone.getCreateTime());
        newGemstone.setUrl(gemstone.getUrl());
        newGemstone.setProduct(newProduct); // Set product cho gemstone mới
        return newGemstone;
    }).collect(Collectors.toList());

    newProduct.setGemstones(gemstones);

    // Set lại Metals cho sản phẩm mới
    if (productsRequest.getMetals() != null) {
        List<Metal> metals = productsRequest.getMetals().stream().map(metalRequest -> {
            Metal metal = new Metal();
            metal.setName(metalRequest.getName());
            metal.setDescription(metalRequest.getDescription());
            metal.setWeight(metalRequest.getWeight());
            metalService.setPricePerWeightUnit(metal);
            metal.setProduct(newProduct);
            return metal;
        }).collect(Collectors.toList());
        newProduct.setMetals(metals);
    }

    // Tính tổng giá của các kim loại
    double totalMetalPrice = 0;
    if (newProduct.getMetals() != null) {
        for (Metal metal : newProduct.getMetals()) {
            double metalPrice = metal.getPricePerWeightUnit();
            totalMetalPrice += metalPrice;
        }
    }

    // Tính tổng giá của các đá quý
    double totalGemstonePrice = 0;
    if (newProduct.getGemstones() != null) {
        totalGemstonePrice = newProduct.getGemstones().stream()
                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                .sum();
    }

    double wage = productsRequest.getWage();

    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = wage + totalPrice + (totalPrice * newProduct.getPriceRate() / 100);
    newProduct.setPrice(totalPriceWithRate);

    // Lưu sản phẩm mới và các thành phần của nó vào cơ sở dữ liệu
    Product savedProduct = productsRepository.save(newProduct);
    if (productsRequest.getGemstones() != null) {
        gemstoneRepository.saveAll(newProduct.getGemstones());
    }
    if (productsRequest.getMetals() != null) {
        metalRepository.saveAll(newProduct.getMetals());
    }

    // Tạo lịch sử sản phẩm
    UpdateProductHistory updateProductHistory = new UpdateProductHistory();
    updateProductHistory.setBarcode(historyProduct.getBarcode());
    updateProductHistory.setCreateTime(historyProduct.getCreateTime());
    updateProductHistory.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    updateProductHistory.setDescriptions("Update product: " + "- Barcode:" + " " + historyProduct.getBarcode() + ", Name: " + historyProduct.getName() +
            ", Category: " + historyProduct.getCategory() + ", PriceRate: " + historyProduct.getPriceRate() +
            ", Stock: " + historyProduct.getStock() + ", Wage: " + historyProduct.getWage() +
            ", Metals: " + historyProduct.getMetals().stream().map(Metal::getName).collect(Collectors.joining(", ")) +
            ", Type When Buy Back: " + historyProduct.getTypeWhenBuyBack() + ", Create Time" + historyProduct.getCreateTime() + ", Update Time" + LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

    // Lưu lịch sử sản phẩm vào cơ sở dữ liệu
    updateProductHistoryRepository.save(updateProductHistory);

    return savedProduct;
}


public Product updateAndCreateNewProductBuyBack(String barcode, ProductsRequest productsRequest) {
    // Tìm kiếm sản phẩm theo barcode
    Optional<Product> existingProductOptional = productsRepository.findByBarcodeAndStatus(barcode, false);
    if (!existingProductOptional.isPresent()) {
        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
    }

    Product existingProduct = existingProductOptional.get();

    // Generate a new unique barcode for the existing product
    String newUniqueBarcode = generateUniqueBarcode( barcode);

    // Set the old product's barcode to the new unique barcode
    existingProduct.setBarcode(newUniqueBarcode);
    existingProduct.setTypeWhenBuyBack(TypeOfProductEnum.PROCESSINGDONE);
    existingProduct.setStatus(false);
    existingProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    productsRepository.save(existingProduct);

    // Tạo sản phẩm mới với thông tin từ request
    Product newProduct = new Product();
    newProduct.setName(productsRequest.getName());
    newProduct.setDescriptions(productsRequest.getDescriptions());
    newProduct.setCategory(productsRequest.getCategory());
    newProduct.setPriceRate(productsRequest.getPriceRate());
    newProduct.setStock(1);
    newProduct.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    newProduct.setTypeWhenBuyBack(productsRequest.getTypeWhenBuyBack());
    newProduct.setStatus(true);
    newProduct.setOldID(String.valueOf(existingProduct.getProductId()));
    newProduct.setBarcode(barcode); // Set barcode cho sản phẩm mới bằng barcode của sản phẩm cũ
    newProduct.setWage(productsRequest.getWage());

    // Set URLs
    if (productsRequest.getUrls() != null) {
        List<ProductUrl> urls = productsRequest.getUrls().stream().map(productUrlRequest -> {
            ProductUrl url = new ProductUrl();
            url.setUrls(productUrlRequest.getUrls());
            url.setProduct(newProduct);
            return url;
        }).collect(Collectors.toList());
        newProduct.setUrls(urls);
    }



    // Tạo danh sách đá quý từ request
    if (productsRequest.getGemstones() != null) {
        List<Gemstone> gemstones = productsRequest.getGemstones().stream().map(gemstoneRequest -> {
            Gemstone gemstone = new Gemstone();
            gemstone.setDescription(gemstoneRequest.getDescription());
            gemstone.setColor(gemstoneRequest.getColor());
            gemstone.setClarity(gemstoneRequest.getClarity());
            gemstone.setCut(gemstoneRequest.getCut());
            gemstone.setCarat(gemstoneRequest.getCarat());
            gemstone.setPrice(gemstoneRequest.getPrice());
            gemstone.setQuantity(gemstoneRequest.getQuantity());
            gemstone.setGemBarcode(gemstoneRequest.getGemBarcode());
            gemstone.setProduct(newProduct);
            return gemstone;
        }).collect(Collectors.toList());
        newProduct.setGemstones(gemstones);
    }
    // Tạo danh sách kim loại từ request
    if (productsRequest.getMetals() != null) {
        List<Metal> metals = productsRequest.getMetals().stream().map(metalRequest -> {
            Metal metal = new Metal();
            metal.setName(metalRequest.getName());
            metal.setDescription(metalRequest.getDescription());
            metal.setWeight(metalRequest.getWeight());
            metalService.setPricePerWeightUnit(metal);
            metal.setProduct(newProduct);
            return metal;
        }).collect(Collectors.toList());
        newProduct.setMetals(metals);
    }

    // Tính tổng giá của các kim loại
    double totalMetalPrice = 0;
    if (newProduct.getMetals() != null) {
        for (Metal metal : newProduct.getMetals()) {
            double metalPrice = metal.getPricePerWeightUnit();
            totalMetalPrice += metalPrice;
        }
    }

    // Tính tổng giá của các đá quý
    double totalGemstonePrice = 0;
    if (newProduct.getGemstones() != null) {
        totalGemstonePrice = newProduct.getGemstones().stream()
                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                .sum();
    }
    double wege = productsRequest.getWage();
    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = wege + totalPrice + (totalPrice * newProduct.getPriceRate() / 100);
    newProduct.setPrice(totalPriceWithRate);

    // Lưu sản phẩm và các thành phần của nó
    Product savedProduct = productsRepository.save(newProduct);
    if (productsRequest.getGemstones() != null) {
        gemstoneRepository.saveAll(newProduct.getGemstones());
    }
    if (productsRequest.getMetals() != null) {
        metalRepository.saveAll(newProduct.getMetals());
    }

    return savedProduct;
} // not use cause this function same function update product


// hàm tà đạo
private String generateUniqueBarcode(String existingBarcode) {
        // Example: Add a prefix "UP:" followed by a unique string to mark an update
        String prefix = "UP:";
        String uniqueString = UUID .randomUUID().toString().replace("-", "");
        return prefix + uniqueString;
    }


////////////------------------------------------------------------------------------/////////////////////
    // bộ lọc tìm kiếm cho fe

  public List<Product> findProductsInPriceRangeAndStatus(double minPrice, double maxPrice) {
        return productsRepository.findByPriceBetweenAndStatus(minPrice, maxPrice,true);
    }

 public List<Product> getAllProductsTrue() {
        List<Product> productList = productsRepository.findByStatus(true);

        if (productList.isEmpty()) {
        throw new ProductNotFoundException("No products found!");
    }
        for (Product product : productList){
            double newPrice = product.getPrice();
            List<Promotion> promotionList = new ArrayList<>();
            for (PromotionProduct promotionProduct : product.getPromotionProducts()){
                promotionList.add(promotionProduct.getPromotion());
            }

            for (Promotion promotion : promotionList) {
                double discountRate = promotion.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        }
        return productList;
    }


    public List<Product> searchProductsByGemstoneAttributes(String color, String clarity, String cut, Double carat) {
        List<Gemstone> gemstones = gemstoneRepository.findAll();

        List<Gemstone> filteredGemstones = gemstones.stream()
                .filter(gemstone -> (color == null || gemstone.getColor().equals(color)) &&
                                    (clarity == null || gemstone.getClarity().equals(clarity)) &&
                                    (cut == null || gemstone.getCut().equals(cut)) &&
                                    (carat == null || gemstone.getCarat() == carat))
                .collect(Collectors.toList());

        return filteredGemstones.stream()
                .map(Gemstone::getProduct)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Product> searchProductsByMetalType(String metalType) {
        List<Metal> metals = metalRepository.findAll();

        List<Metal> filteredMetals = metals.stream()
                .filter(metal -> metal.getTypeOfMetal().getMetalType().equals(metalType))
                .collect(Collectors.toList());

        return filteredMetals.stream()
                .map(Metal::getProduct)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<Product> searchProductsByName(String name) {
        return productsRepository.findByNameContaining(name);
    }


     public List<Product> getProductsByTypeWhenBuyBack(TypeOfProductEnum typeWhenBuyBack) {
        return productsRepository.findByTypeWhenBuyBack(typeWhenBuyBack);
    }

   public Product getProductById(Long productId) {
    Product product = productsRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

    double newPrice = calculateNewPrice(product);
    product.setNewPrice(newPrice);

    return product;
}

public List<Product> getProductsByCategory(TypeEnum category) {
    List<Product> products = productsRepository.findByCategory(category);

    for (Product product : products) {
        double newPrice = calculateNewPrice(product);
        product.setNewPrice(newPrice);
    }

    return products;
}

public List<Gemstone> findGemByBarCodes(List<String> barcodes) {
    List<Gemstone> gemstones = gemstoneRepository.findByBarcodes(barcodes);

    // Assume gemstones have a relation to products and you want to update the new price for each product
    for (Gemstone gemstone : gemstones) {
        Product product = gemstone.getProduct();
        if (product != null) {
            double newPrice = calculateNewPrice(product);
            product.setNewPrice(newPrice);
        }
    }

    return gemstones;
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
@Transactional
public void deleteListGem(List<String> barcodes) {
    // Tìm kiếm các Gemstone bằng barcode
    List<Gemstone> gemstonesToDelete = gemstoneRepository.findByBarcodes(barcodes);

    // Xóa các Gemstone đã tìm thấy
    gemstoneRepository.deleteAll(gemstonesToDelete);
}

public void detachGemstonesByProductBarcode(Long id) {
    Product product = productsRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

    Long productId = product.getProductId();
    detachGemstonesByProductId(productId);
    updateGemstoneStatus();
    updateProductPrice(product); // Tính lại giá sau khi tháo đá quý
}

    private void detachGemstonesByProductId(Long productId) {
        gemstoneRepository.detachFromProductById(productId, GemStatus.NOTUSE);
    }

     public boolean checkGemstoneStatus(String gemBarcode) {
        return gemstoneRepository.existsByGemBarcodeAndUserStatusNot(gemBarcode, GemStatus.NOTUSE);
    }

    private void updateProductPrice(Product product) {
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
                .filter(gemstone -> gemstone.getUserStatus() == GemStatus.USE) // Chỉ tính những đá quý đang được sử dụng
                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                .sum();
    }

    double wage = product.getWage();

    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = wage + totalPrice + (totalPrice * product.getPriceRate() / 100);
    product.setPrice(totalPriceWithRate);
    product.setStatus(false);

    // Cập nhật sản phẩm trong cơ sở dữ liệu
    productsRepository.save(product);
}

private void updateGemstoneStatus() {
        List<Gemstone> gemstones = gemstoneRepository.findGemstonesWithNullProductAndUserStatusUse(GemStatus.USE);
        for (Gemstone gemstone : gemstones) {
            gemstone.setUserStatus(GemStatus.NOTUSE);
        }
        gemstoneRepository.saveAll(gemstones);
    }

    @Transactional
public void unlinkGemsByProductBarcode(String barcode) {
    // Tìm sản phẩm theo barcode
    Optional<Product> productOptional = productsRepository.findByBarcodeAndStatus(barcode, true);
    if (!productOptional.isPresent()) {
        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
    }

    Product product = productOptional.get();
    Long productId = product.getProductId();

    deletePromotionProductsByBarcode(barcode);


    // Tạo bản sao của sản phẩm
    Product historyProduct = new Product();
    historyProduct.setName(product.getName());
    historyProduct.setDescriptions(product.getDescriptions());
    historyProduct.setCategory(product.getCategory());
    historyProduct.setPriceRate(product.getPriceRate());
    historyProduct.setStock(product.getStock());
    historyProduct.setTypeWhenBuyBack(product.getTypeWhenBuyBack());
    historyProduct.setUpdateTime(product.getUpdateTime());
    historyProduct.setStatus(false);
    historyProduct.setBarcode(product.getBarcode());
    historyProduct.setWage(product.getWage());
    historyProduct.setOldID(String.valueOf(product.getProductId()));

    // Tạo bản sao của URLs
    if (product.getUrls() != null) {
        List<ProductUrl> oldUrls = product.getUrls().stream().map(url -> {
            ProductUrl oldUrl = new ProductUrl();
            oldUrl.setUrls(url.getUrls());
            oldUrl.setProduct(historyProduct);
            return oldUrl;
        }).collect(Collectors.toList());
        historyProduct.setUrls(oldUrls);
    }

    // Tạo bản sao của Gemstones cũ và gán vào historyProduct
    if (product.getGemstones() != null) {
        List<Gemstone> oldGemstones = product.getGemstones().stream().map(gemstone -> {
            Gemstone oldGemstone = new Gemstone();
            oldGemstone.setGemBarcode(gemstone.getGemBarcode());
            oldGemstone.setDescription(gemstone.getDescription());
            oldGemstone.setColor(gemstone.getColor());
            oldGemstone.setClarity(gemstone.getClarity());
            oldGemstone.setCut(gemstone.getCut());
            oldGemstone.setCarat(gemstone.getCarat());
            oldGemstone.setPrice(gemstone.getPrice());
            oldGemstone.setQuantity(gemstone.getQuantity());
            oldGemstone.setUserStatus(GemStatus.FALSE); // Set trạng thái là FALSE
            oldGemstone.setUrl(gemstone.getUrl());
            oldGemstone.setUpdateTime(gemstone.getUpdateTime());
            oldGemstone.setProduct(historyProduct); // Set product cho gemstone cũ
            return oldGemstone;
        }).collect(Collectors.toList());
        historyProduct.setGemstones(oldGemstones);
    }

    // Set Metals cho sản phẩm cũ
    if (product.getMetals() != null) {
        List<Metal> oldMetals = product.getMetals().stream().map(metal -> {
            Metal oldMetal = new Metal();
            oldMetal.setName(metal.getName());
            oldMetal.setDescription(metal.getDescription());
            oldMetal.setWeight(metal.getWeight());
            oldMetal.setPricePerWeightUnit(metal.getPricePerWeightUnit());
            oldMetal.setProduct(historyProduct); // Set product cho metal cũ
            return oldMetal;
        }).collect(Collectors.toList());
        historyProduct.setMetals(oldMetals);
    }

    // Tạo bản ghi lịch sử cho sản phẩm
    UpdateProductHistory updateProductHistory = new UpdateProductHistory();
    updateProductHistory.setBarcode(historyProduct.getBarcode());
    updateProductHistory.setCreateTime(historyProduct.getCreateTime());
    updateProductHistory.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    updateProductHistory.setDescriptions("Unlink gems and delete promotion from product: " +
            "- Barcode: " + historyProduct.getBarcode() +
            ", Name: " + historyProduct.getName() +
            ", Category: " + historyProduct.getCategory() +
            ", PriceRate: " + historyProduct.getPriceRate() +
            ", Stock: " + historyProduct.getStock() +
            ", Wage: " + historyProduct.getWage() +
            ", Metals: " + historyProduct.getMetals().stream().map(Metal::getName).collect(Collectors.joining(", ")) +
            ", TypeWhenBuyBack: " + historyProduct.getTypeWhenBuyBack()
            + historyProduct.getTypeWhenBuyBack() + ", Create Time" + historyProduct.getCreateTime() + ", Update Time" + LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
    );

    // Lưu lịch sử sản phẩm vào cơ sở dữ liệu
    updateProductHistoryRepository.save(updateProductHistory);

    // Gỡ liên kết cho các gemstone có trạng thái là USE
    gemstoneRepository.detachGemsByProductId(productId, GemStatus.USE, GemStatus.NOTUSE);
}



     public void validateGemBarcodes(List<String> gemBarcodes) {
        if (gemBarcodes == null) {
            // Nếu danh sách barcode là null, không thực hiện bất kỳ kiểm tra nào và kết thúc hàm
            return;
        }

        // Loại bỏ các giá trị null và trống trong danh sách barcode
        List<String> validBarcodes = gemBarcodes.stream()
                .filter(barcode -> barcode != null && !barcode.trim().isEmpty())
                .collect(Collectors.toList());

        if (validBarcodes.isEmpty()) {
            // Nếu sau khi lọc danh sách barcode không còn giá trị hợp lệ, không thực hiện kiểm tra và kết thúc hàm
            return;
        }

        // Kiểm tra các barcode gem không hợp lệ hoặc đã sử dụng
        List<Gemstone> invalidOrUsedGemstones = gemstoneRepository.findInvalidOrUsedGemstones(validBarcodes, GemStatus.NOTUSE);

        if (!invalidOrUsedGemstones.isEmpty()) {
            String invalidBarcodes = invalidOrUsedGemstones.stream()
                    .map(Gemstone::getGemBarcode)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid or used gemstones with barcodes: " + invalidBarcodes);
        }
    }


    public void deletePromotionProductsByBarcode(String barcode) {
        if (barcode != null && !barcode.trim().isEmpty()) {
            promotionProductRepository.deleteByProductBarcode(barcode);
        }
    }

}
