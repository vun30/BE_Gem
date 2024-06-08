package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.MetalPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MetalPriceRepository extends JpaRepository<MetalPrice, Long> {
    Optional<MetalPrice> findByMetalTypeAndStatus(String metalType, boolean status);
    List<MetalPrice> findByUpdateDateBefore(LocalDateTime  updateDate);
}
