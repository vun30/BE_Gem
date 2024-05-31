package online.gemfpt.BE.Repository;

import online.gemfpt.BE.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product,Long> {
}
