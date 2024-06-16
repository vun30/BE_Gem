package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Gemstone;
import online.gemfpt.BE.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GemstoneRepository extends JpaRepository<Gemstone, Long> {
     @EntityGraph(attributePaths = "gemProperties")
     List<Gemstone> findByProduct(Product product);
}

