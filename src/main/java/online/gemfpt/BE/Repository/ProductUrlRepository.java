package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.ProductUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUrlRepository extends JpaRepository<ProductUrl, Long> {
}
