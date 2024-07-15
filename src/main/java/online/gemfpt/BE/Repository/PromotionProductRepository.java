package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import online.gemfpt.BE.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    List<PromotionProduct> findByProductAndIsActive(Product product, boolean isActive);
    List<PromotionProduct> findByPromotion(Promotion promotion);
    List<PromotionProduct> findByProduct(Product product);
}
