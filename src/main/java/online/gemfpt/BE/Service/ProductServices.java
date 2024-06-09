package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.MetalPriceRepository;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.Repository.GemstoneRepository;
import online.gemfpt.BE.Repository.MetalRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
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
//    public Product creates(ProductsRequest productsRequest) {
//        // Kiểm tra xem sản phẩm có tồn tại không
//        Optional<Product> existProduct = productsRepository.findByBarcode(productsRequest.getBarcode());
//        if (existProduct.isPresent()) {
//            throw new IllegalArgumentException("Barcode already exists!");
//        }
//
//        // Tạo mới một sản phẩm
//        Product product = new Product();
//        product.setName(productsRequest.getName());
//        product.setDescriptions(productsRequest.getDescriptions());
//        product.setCategory(productsRequest.getCategory());
//        product.setPriceRate(productsRequest.getPriceRate());
//        product.setStock(productsRequest.getStock());
//        product.setUrl(productsRequest.getUrl());
//        product.setCreateTime(LocalDateTime.now());
//        product.setStatus(true);
//        product.setBarcode(productsRequest.getBarcode());
//
//        // Tạo danh sách kim loại từ request
//        if (productsRequest.getMetals() != null) {
//            List<Metal> metals = productsRequest.getMetals().stream().map(metalRequest -> {
//                Metal metal = new Metal();
//                metal.setName(metalRequest.getName());
//                metal.setDescription(metalRequest.getDescription());
//                metal.setWeight(metalRequest.getWeight());
//                metal.setPricePerWeightUnit(metalRequest.getPricePerWeightUnit());
//                metal.setProduct(product);
//                return metal;
//            }).collect(Collectors.toList());
//            product.setMetals(metals);
//        }
//
//        // Tạo danh sách đá quý từ request
//        if (productsRequest.getGemstones() != null) {
//            List<Gemstone> gemstones = productsRequest.getGemstones().stream().map(gemstoneRequest -> {
//                Gemstone gemstone = new Gemstone();
//                gemstone.setDescription(gemstoneRequest.getDescription());
//                gemstone.setPrice(gemstoneRequest.getPrice());
//                gemstone.setQuantity(gemstoneRequest.getQuantity());
//                gemstone.setProduct(product);
//                return gemstone;
//            }).collect(Collectors.toList());
//            product.setGemstones(gemstones);
//        }
//
//        // Tính tổng giá của các thành phần
//        double totalGemstonePrice = product.getGemstones().stream()
//                .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
//                .sum();
//
//        double totalMetalPrice = product.getMetals().stream()
//                .mapToDouble(metal -> metal.getPricePerWeightUnit() * metal.getWeight())
//                .sum();
//
//        // Tính giá cuối cùng của sản phẩm
//        double totalPrice = (totalGemstonePrice + totalMetalPrice)  ;
//        double totalPrice2 = totalPrice + (totalPrice * (product.getPriceRate() / 100 ));
//        product.setPrice(totalPrice2);
//
//        // Lưu sản phẩm và các thành phần của nó
//        Product savedProduct = productsRepository.save(product);
//        if (productsRequest.getGemstones() != null) {
//            gemstoneRepository.saveAll(product.getGemstones());
//        }
//        if (productsRequest.getMetals() != null) {
//            metalRepository.saveAll(product.getMetals());
//        }
//        return savedProduct;
//    }
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
    product.setStock(productsRequest.getStock());
    product.setUrl(productsRequest.getUrl());
    product.setCreateTime(LocalDateTime.now());
    product.setStatus(true);
    product.setBarcode(productsRequest.getBarcode());

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

    // Tạo danh sách đá quý từ request
    if (productsRequest.getGemstones() != null) {
        List<Gemstone> gemstones = productsRequest.getGemstones().stream().map(gemstoneRequest -> {
            Gemstone gemstone = new Gemstone();
            gemstone.setDescription(gemstoneRequest.getDescription());
            gemstone.setPrice(gemstoneRequest.getPrice());
            gemstone.setQuantity(gemstoneRequest.getQuantity());
            gemstone.setProduct(product);
            return gemstone;
        }).collect(Collectors.toList());
        product.setGemstones(gemstones);
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


    public List<Product> getAllProducts() {
        List<Product> productList = productsRepository.findAll();
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

    public Product getProductByBarcode(Long barcode) {
        Optional<Product> optionalProduct = productsRepository.findById(barcode);
        return optionalProduct.orElse(null);
    }

    public Product updateProductByBarcode(ProductsRequest productsRequest) {

        Optional<Product> optionalProduct = productsRepository.findByBarcode(productsRequest.getBarcode());
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setDescriptions(productsRequest.getDescriptions().isEmpty() ? product.getDescriptions() : productsRequest.getDescriptions());
            product.setName(productsRequest.getName().isEmpty() ? product.getName() : productsRequest.getName());
            product.setCategory(productsRequest.getCategory().isEmpty() ? product.getCategory() : productsRequest.getCategory());
            product.setPrice(productsRequest.getPrice() == 0 ? product.getPrice() : productsRequest.getPrice());
            product.setPriceRate(productsRequest.getPriceRate() == 0 ? product.getPriceRate() : productsRequest.getPriceRate());
            product.setStock(productsRequest.getStock() == 0 ? product.getStock() : productsRequest.getStock());
            product.setUrl(productsRequest.getUrl().isEmpty() ? product.getUrl() : productsRequest.getUrl());
            product.setBarcode(productsRequest.getBarcode());
            product.setUpdateTime(LocalDateTime.now());

            return productsRepository.save(product);
        } else {
            return null;
        }
    }

    public Product toggleProductActive(String barcode) {
        Product product = productsRepository.findByBarcode(barcode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setStatus(!product.isStatus()); // Đảo ngược trạng thái hiện tại
        return productsRepository.save(product);
    }
}
