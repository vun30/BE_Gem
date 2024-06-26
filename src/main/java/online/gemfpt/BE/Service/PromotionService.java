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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductsRepository productRepository;

    @Autowired
    private PromotionProductRepository promotionProductRepository;

    public List<Promotion> getAllDiscount(){
        List<Promotion> promotions = promotionRepository.findAll();
        for(Promotion promotion : promotions){
            List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
            promotion.setPromotionProducts(promotionProducts);
        }
        return promotions;
    }

    public Promotion findDiscountByID(Long disID){
        Optional<Promotion> discount = promotionRepository.findById(disID);
        return discount.orElse(null);
    }

    public Promotion createDiscount(PromotionRequestForBarcode discountRequest){
        List <Product> productList = new ArrayList<>();
        for (String barcode : discountRequest.getBarcode()) {
            Optional<Product> product = productRepository.findByBarcode(barcode);
            if (product.isEmpty()) {
                throw new BadRequestException("Barcode don't exists!");
            }
            productList.add(product.get());
        }

        Promotion promotion = new Promotion();
        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts(discountRequest.getApplicableProducts());
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

    public Promotion createDiscountForCategory(PromotionCreateRequest discountRequest, TypeEnum category){
        List<Product> productList = productRepository.findByCategory(category);
        if (productList.isEmpty()) {
            throw new BadRequestException("No products found in the specified category.");
        }

        Promotion promotion = new Promotion();

        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts(String.valueOf(category));
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

    public Promotion createDiscountForAllProducts(PromotionCreateRequest discountRequest){
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new BadRequestException("No products found in the inventory.");
        }

        Promotion promotion = new Promotion();
        promotion.setProgramName(discountRequest.getProgramName());
        promotion.setDiscountRate(discountRequest.getDiscountRate());
        promotion.setDescription(discountRequest.getDescription());
        promotion.setApplicableProducts("All Products");
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

    public Promotion updatePromotion(PromotionUpdateRequest discountRequest, Long promotionId){
        Optional<Promotion> discountExist = promotionRepository.findById(promotionId);
        if(discountExist.isPresent()){
            Promotion promotion = discountExist.get();
            promotion.setProgramName(discountRequest.getProgramName().isEmpty() ? promotion.getProgramName() : discountRequest.getProgramName());
            promotion.setDiscountRate(discountRequest.getDiscountRate() == 0 ? promotion.getDiscountRate() : discountRequest.getDiscountRate());
            promotion.setDescription(discountRequest.getDescription().isEmpty() ? promotion.getDescription() : discountRequest.getDescription());
            promotion.setEndTime(LocalDateTime.now());

            return promotionRepository.save(promotion);
        }else{
            return null;
        }
    }

    public Promotion discountStatus(Long disID){
        Promotion promotion = promotionRepository.findById(disID).orElseThrow(() -> new EntityNotFoundException("Discount not found"));
        promotion.setStatus(!promotion.isStatus());

        List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
        for(PromotionProduct promotionProduct : promotionProducts){
            promotionProduct.setActive(promotion.isStatus());
            promotionProductRepository.save(promotionProduct);
        }
        return promotionRepository.save(promotion);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Chạy hàng ngày vào lúc nửa đêm
    @Transactional
    public void updatePromotionStatusBasedOnEndTime() {
        List<Promotion> promotions = promotionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Promotion promotion : promotions) {
            if (promotion.getEndTime().isBefore(now) && promotion.isStatus()) {
                promotion.setStatus(false);
                List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
                for (PromotionProduct promotionProduct : promotionProducts) {
                    promotionProduct.setActive(false);
                    promotionProductRepository.save(promotionProduct);
                }
                promotionRepository.save(promotion);
            }
        }
    }

}
