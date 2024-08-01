package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.PromotionProductRepository;
import online.gemfpt.BE.Repository.PromotionRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.model.PromotionRequest.PromotionUpdateRequest;
import online.gemfpt.BE.model.PromotionRequest.PromotionCreateRequest;
import online.gemfpt.BE.model.PromotionRequest.PromotionRequestForBarcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@SpringBootApplication
@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductsRepository productRepository;

    @Autowired
    private PromotionProductRepository promotionProductRepository;

    public List<Promotion> getAllDiscount() {
        List<Promotion> promotions = promotionRepository.findAll();
        for (Promotion promotion : promotions) {
            List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
            promotion.setPromotionProducts(promotionProducts);
        }
        return promotions;
    }

    public Promotion findDiscountByID(Long disID) {
        Optional<Promotion> discount = promotionRepository.findById(disID);
        return discount.orElse(null);
    }

    public Promotion createDiscount(PromotionRequestForBarcode discountRequest) {
        List<Product> productList = new ArrayList<>();
        List<String> existingBarcodes = new ArrayList<>();
        Set<String> addedBarcodes = new HashSet<>();

        for (String barcode : discountRequest.getBarcode()) {
            if (!addedBarcodes.add(barcode)) {
                throw new BadRequestException("Barcode " + barcode + " is already added in this promotion creation.");
            }
            Optional<Product> product = productRepository.findByBarcode(barcode);
            if (product.isEmpty()) {
                throw new BadRequestException("Barcode doesn't exist!");
            }
            List<PromotionProduct> existingPromotions = promotionProductRepository.findByProduct(product.get());
            boolean hasActivePromotion = existingPromotions.stream()
                    .anyMatch(promotionProduct -> promotionProduct.isActive() && promotionProduct.getPromotion().isStatus());
            if (hasActivePromotion) {
                existingBarcodes.add(barcode);
            } else {
                productList.add(product.get());
            }
        }

        if (!existingBarcodes.isEmpty()) {
            throw new BadRequestException("The following barcodes are already in another active promotion: " + String.join(", ", existingBarcodes));
        }

        Promotion promotion = new Promotion();
        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts("Barcode");
        promotion.setStartTime(LocalDateTime.now());
        promotion.setEndTime(discountRequest.getEndTime());
        promotion.setStatus(true);

        promotionRepository.save(promotion);

        for (Product product : productList) {
            PromotionProduct promotionProduct = new PromotionProduct();
            promotionProduct.setProduct(product);
            promotionProduct.setPromotion(promotion);
            promotionProduct.setDiscountValue(discountRequest.getDiscountRate());
            promotionProduct.setActive(true);

            promotionProductRepository.save(promotionProduct);
        }

        return promotion;
    }

    public Promotion createDiscountForCategory(PromotionCreateRequest discountRequest, TypeEnum category) {
        List<Product> productList = productRepository.findByCategory(category);
        if (productList.isEmpty()) {
            throw new BadRequestException("No products found in the specified category.");
        }

        List<String> existingBarcodes = new ArrayList<>();
        Set<String> addedBarcodes = new HashSet<>();

        for (Product product : productList) {
            if (!addedBarcodes.add(product.getBarcode())) {
                throw new BadRequestException("Barcode " + product.getBarcode() + " is already added in this promotion creation.");
            }
            List<PromotionProduct> existingPromotions = promotionProductRepository.findByProduct(product);
            boolean hasActivePromotion = existingPromotions.stream()
                    .anyMatch(promotionProduct -> promotionProduct.isActive() && promotionProduct.getPromotion().isStatus());
            if (hasActivePromotion) {
                existingBarcodes.add(product.getBarcode());
            }
        }

        if (!existingBarcodes.isEmpty()) {
            throw new BadRequestException("The following barcodes are already in another active promotion: " + String.join(", ", existingBarcodes));
        }

        Promotion promotion = new Promotion();
        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts(String.valueOf(category));
        promotion.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        promotion.setEndTime(discountRequest.getEndTime());
        promotion.setStatus(true);

        promotionRepository.save(promotion);

        for (Product product : productList) {
            PromotionProduct promotionProduct = new PromotionProduct();
            promotionProduct.setProduct(product);
            promotionProduct.setPromotion(promotion);
            promotionProduct.setDiscountValue(discountRequest.getDiscountRate());
            promotionProduct.setActive(true);

            promotionProductRepository.save(promotionProduct);
        }

        return promotion;
    }

    public Promotion createDiscountForAllProducts(PromotionCreateRequest discountRequest) {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new BadRequestException("No products found in the inventory.");
        }

        List<String> existingBarcodes = new ArrayList<>();
        Set<String> addedBarcodes = new HashSet<>();

        for (Product product : productList) {
            if (!addedBarcodes.add(product.getBarcode())) {
                throw new BadRequestException("Barcode " + product.getBarcode() + " is already added in this promotion creation.");
            }
            List<PromotionProduct> existingPromotions = promotionProductRepository.findByProduct(product);
            boolean hasActivePromotion = existingPromotions.stream()
                    .anyMatch(promotionProduct -> promotionProduct.isActive() && promotionProduct.getPromotion().isStatus());
            if (hasActivePromotion) {
                existingBarcodes.add(product.getBarcode());
            }
        }

        if (!existingBarcodes.isEmpty()) {
            throw new BadRequestException("The following barcodes are already in another active promotion: " + String.join(", ", existingBarcodes));
        }

        Promotion promotion = new Promotion();
        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts("All Products");
        promotion.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        promotion.setEndTime(discountRequest.getEndTime());
        promotion.setStatus(true);

        promotionRepository.save(promotion);

        for (Product product : productList) {
            PromotionProduct promotionProduct = new PromotionProduct();
            promotionProduct.setProduct(product);
            promotionProduct.setPromotion(promotion);
            promotionProduct.setDiscountValue(discountRequest.getDiscountRate());
            promotionProduct.setActive(true);

            promotionProductRepository.save(promotionProduct);
        }

        return promotion;
    }

    public Promotion updatePromotion(PromotionUpdateRequest discountRequest, Long promotionId) {
        Optional<Promotion> discountExist = promotionRepository.findById(promotionId);
        if (discountExist.isPresent()) {
            Promotion promotion = discountExist.get();
            promotion.setProgramName(discountRequest.getProgramName().isEmpty() ? promotion.getProgramName() : discountRequest.getProgramName());
            promotion.setDiscountRate(discountRequest.getDiscountRate() == 0 ? promotion.getDiscountRate() : discountRequest.getDiscountRate());
            promotion.setDescription(discountRequest.getDescription().isEmpty() ? promotion.getDescription() : discountRequest.getDescription());
            promotion.setApplicableProducts(discountRequest.getApplicableProducts().isEmpty() ? promotion.getApplicableProducts() : discountRequest.getApplicableProducts());
            promotion.setEndTime(discountRequest.getEndTime().isBefore(promotion.getStartTime()) ? promotion.getEndTime() : promotion.getEndTime());

            List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
            for (PromotionProduct promotionProduct : promotionProducts) {
                promotionProduct.setDiscountValue(promotion.getDiscountRate());
                Product product = promotionProduct.getProduct();
                product.setNewPrice(product.getPrice() * (1 - promotion.getDiscountRate() / 100));
                promotionProductRepository.save(promotionProduct);
                productRepository.save(product);
            }

            return promotionRepository.save(promotion);
        } else {
            throw new EntityNotFoundException("Promotion not found");
        }
    }

    public Promotion discountStatus(Long disID) {
        Promotion promotion = promotionRepository.findById(disID).orElseThrow(() -> new EntityNotFoundException("Discount not found"));
        promotion.setStatus(!promotion.isStatus());

        List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
        for (PromotionProduct promotionProduct : promotionProducts) {
            Product product = promotionProduct.getProduct();
            if (!promotion.isStatus()) {
                product.setNewPrice(null);
            } else {
                product.setNewPrice(product.getPrice() * (1 - promotion.getDiscountRate() / 100));
            }
            promotionProduct.setActive(promotion.isStatus());
            productRepository.save(product);
            promotionProductRepository.save(promotionProduct);
        }

        return promotionRepository.save(promotion);
    }
}
