package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByBarcode(String barcode);
}
