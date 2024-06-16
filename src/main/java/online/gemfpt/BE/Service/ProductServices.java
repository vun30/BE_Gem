package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.model.MetalRequest;
import online.gemfpt.BE.model.ProductUrlRequest;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    Optional<Product> existProduct = productsRepository.findByBarcode(productsRequest.getBarcode());
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

    // Helper method to calculate total metal price
    private double calculateTotalMetalPrice(List<Metal> metals) {
        return metals.stream()
                .mapToDouble(metal -> metal.getPricePerWeightUnit())
                .sum();
    }

    // Helper method to calculate total gemstone price
    private double calculateTotalGemstonePrice(List<Gemstone> gemstones) {
        return gemstones.stream()
                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                .sum();
    }


     public Product getProductByBarcode(String barcode) {
        Optional<Product> optionalProduct = productsRepository.findByBarcode(barcode);
        return optionalProduct.orElse(null); // Trả về null nếu không tìm thấy sản phẩm
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
            List<Discount> discountList = new ArrayList<>();
            for (DiscountProduct discountProduct : product.getDiscountProducts()){
                discountList.add(discountProduct.getDiscount());
            }

            for (Discount discount : discountList) {
                double discountRate = discount.getDiscountRate() / 100;
                newPrice = newPrice - (product.getPrice() * discountRate);
            }
            product.setNewPrice(newPrice);
        }
        return productList;
    }
@Transactional
public Product updateProduct(String barcode, ProductsRequest productsRequest) {
    Product product = productsRepository.findByBarcode(barcode)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with barcode: " + barcode));

    // Cập nhật các thông tin chung của sản phẩm nếu có trong yêu cầu
    if (productsRequest.getName() != null) {
        product.setName(productsRequest.getName());
    }
    if (productsRequest.getDescriptions() != null) {
        product.setDescriptions(productsRequest.getDescriptions());
    }
    if (productsRequest.getCategory() != null) {
        product.setCategory(productsRequest.getCategory());
    }
    if (productsRequest.getPriceRate() != 0) {
        product.setPriceRate(productsRequest.getPriceRate());
    }


    // Cập nhật danh sách URLs từ request nếu có
    if (productsRequest.getUrls() != null) {
        List<ProductUrl> urls = productsRequest.getUrls().stream().map(productUrlRequest -> {
            ProductUrl url = new ProductUrl();
            url.setUrls(productUrlRequest.getUrls());
            url.setProduct(product);
            return url;
        }).collect(Collectors.toList());
        product.setUrls(urls);
    }

    // Cập nhật danh sách đá quý từ request nếu có
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
            gemstone.setCertificateCode(gemstoneRequest.getCertificateCode());
            gemstone.setProduct(product);
            return gemstone;
        }).collect(Collectors.toList());
        product.setGemstones(gemstones);
    }

    // Cập nhật danh sách kim loại từ request nếu có
if (productsRequest.getMetals() != null) {
    for (MetalRequest metalRequest : productsRequest.getMetals()) {
        Optional<Metal> optionalMetal = product.getMetals().stream()
                .filter(m -> m.getName() == metalRequest.getName())
                .findFirst();

        Metal metal;
        if (optionalMetal.isPresent()) {
            metal = optionalMetal.get();
        } else {
            metal = new Metal();
            metal.setProduct(product);
            product.getMetals().add(metal);
        }

        if (metalRequest.getName() != null) {
            metal.setName(metalRequest.getName());
        }
        if (metalRequest.getDescription() != null) {
            metal.setDescription(metalRequest.getDescription());
        }
        if (metalRequest.getWeight() != 0) {
            metal.setWeight(metalRequest.getWeight());
        }
        // Set price per weight unit nếu cần thiết, ví dụ như gọi một hàm trong service
        metalService.setPricePerWeightUnit(metal);
    }
}

    // Tính lại giá sản phẩm nếu có thay đổi
    double totalMetalPrice = calculateTotalMetalPrice(product.getMetals());
    double totalGemstonePrice = calculateTotalGemstonePrice(product.getGemstones());
    double totalPrice = totalMetalPrice + totalGemstonePrice;
    double totalPriceWithRate = totalPrice + (totalPrice * product.getPriceRate() / 100);
    product.setPrice(totalPriceWithRate);

    product.setUpdateTime(LocalDateTime.now());

    return productsRepository.save(product);
}



}



