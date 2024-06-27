package online.gemfpt.BE.config;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.PromotionProductRepository;
import online.gemfpt.BE.Repository.PromotionRepository;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class CronJobConfig {
    @Autowired
    PromotionProductRepository promotionProductRepository;

    @Autowired
    PromotionRepository promotionRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Chạy hàng ngày vào lúc nửa đêm
    @Transactional
    public void updatePromotionStatusBasedOnEndTime() {
        System.out.println("running");
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
