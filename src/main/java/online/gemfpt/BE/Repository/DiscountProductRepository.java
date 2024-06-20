package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.DiscountProduct;
import online.gemfpt.BE.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountProductRepository extends JpaRepository<DiscountProduct, Long> {
    List<DiscountProduct> findByProductAndIsActive(Product product, boolean isActive);
}
