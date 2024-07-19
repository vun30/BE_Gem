package online.gemfpt.BE.Repository;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import online.gemfpt.BE.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    List<PromotionProduct> findByProductAndIsActive(Product product, boolean isActive);
    List<PromotionProduct> findByPromotion(Promotion promotion);
    List<PromotionProduct> findByProduct(Product product);


    @Modifying
    @Transactional
    @Query("DELETE FROM PromotionProduct pp WHERE pp.product.barcode = :barcode")
    void deleteByProductBarcode(@Param("barcode") String barcode);
}
