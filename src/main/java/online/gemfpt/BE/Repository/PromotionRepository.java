package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    Optional<Promotion> findById(long id);
}
