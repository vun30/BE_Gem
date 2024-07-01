package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.model.ProductUrlRequest;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    product.setCreateTime(LocalDateTime.now());
    product.setStatus(true);
    product.setBarcode(productsRequest.getBarcode());

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
            gemstone.setProduct(product);
            return gemstone;
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


    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
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

//    // Helper method to calculate total metal price
//    private double calculateTotalMetalPrice(List<Metal> metals) {
//        return metals.stream()
//                .mapToDouble(metal -> metal.getPricePerWeightUnit())
//                .sum();
//    }
//
//    // Helper method to calculate total gemstone price
//    private double calculateTotalGemstonePrice(List<Gemstone> gemstones) {
//        return gemstones.stream()
//                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
//                .sum();
//    }


 public Product getProductByBarcode(String barcode) {
    Optional<Product> optionalProduct = productsRepository.findByBarcodeAndStatus(barcode,true);
    if (!optionalProduct.isPresent()) {
        throw new ProductNotFoundException("Product not found with barcode: " + barcode);
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
    Optional<Product> existingProductOptional = productsRepository.findByBarcodeAndStatus(barcode,true);
    if (!existingProductOptional.isPresent()) {
        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
    }

     Product existingProduct = existingProductOptional.get();

    // Chuyển trạng thái sản phẩm hiện tại thành false
    existingProduct.setStatus(false);
    existingProduct.setUpdateTime(LocalDateTime.now());
    productsRepository.save(existingProduct);

    // Lấy barcode hiện tại của existingProduct
    String currentBarcode = existingProduct.getBarcode();

    // Kiểm tra nếu có dấu '|' trong barcode thì chỉ lấy phần sau dấu '|'
    int indexOfPipe = currentBarcode.indexOf("|");
    if (indexOfPipe != -1) {
        currentBarcode = currentBarcode.substring(indexOfPipe + 1);
    }

    // Tạo sản phẩm mới với thông tin từ request
    Product newProduct = new Product();

    newProduct.setName( productsRequest.getName());
    newProduct.setDescriptions(productsRequest.getDescriptions());
    newProduct.setCategory(productsRequest.getCategory());
    newProduct.setPriceRate(productsRequest.getPriceRate());
    newProduct.setStock(1);
    newProduct.setUpdateTime(LocalDateTime.now());
    newProduct.setStatus(true);
    newProduct.setOldID(String.valueOf(existingProduct.getProductId()));
     newProduct.setBarcode(generateUniqueBarcode(currentBarcode) +"|"+ existingProduct.getBarcode()); // Set barcode cho sản phẩm mới

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

    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = totalPrice + (totalPrice * newProduct.getPriceRate() / 100);
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
}

public Product updateAndCreateNewProductBuyBack(String barcode, ProductsRequest productsRequest) {
    // Tìm kiếm sản phẩm theo barcode
    Optional<Product> existingProductOptional = productsRepository.findByBarcodeAndStatus(barcode,false);
    if (!existingProductOptional.isPresent()) {
        throw new EntityNotFoundException("Product not found with barcode: " + barcode);
    }

     Product existingProduct = existingProductOptional.get();

    // Chuyển trạng thái sản phẩm hiện tại thành false
    existingProduct.setTypeWhenBuyBack(TypeOfProductEnum.PROCESSINGDONE);
    existingProduct.setStatus(false);
    existingProduct.setUpdateTime(LocalDateTime.now());
    productsRepository.save(existingProduct);

    // Lấy barcode hiện tại của existingProduct
    String currentBarcode = existingProduct.getBarcode();

    // Kiểm tra nếu có dấu '|' trong barcode thì chỉ lấy phần sau dấu '|'
    int indexOfPipe = currentBarcode.indexOf("|");
    if (indexOfPipe != -1) {
        currentBarcode = currentBarcode.substring(indexOfPipe + 1);
    }

    // Tạo sản phẩm mới với thông tin từ request
    Product newProduct = new Product();

    newProduct.setName( productsRequest.getName());
    newProduct.setDescriptions(productsRequest.getDescriptions());
    newProduct.setCategory(productsRequest.getCategory());
    newProduct.setPriceRate(productsRequest.getPriceRate());
    newProduct.setStock(1);
    newProduct.setUpdateTime(LocalDateTime.now());
    newProduct.setTypeWhenBuyBack(productsRequest.getTypeWhenBuyBack());
    newProduct.setStatus(true);
    newProduct.setOldID(String.valueOf(existingProduct.getProductId()));
    newProduct.setBarcode(generateUniqueBarcode(currentBarcode) +"|"+ existingProduct.getBarcode()); // Set barcode cho sản phẩm mới

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

    // Tính giá cuối cùng của sản phẩm
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = totalPrice + (totalPrice * newProduct.getPriceRate() / 100);
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
}


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

}
