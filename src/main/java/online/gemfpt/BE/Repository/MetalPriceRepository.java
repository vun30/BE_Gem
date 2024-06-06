package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.MetalPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetalPriceRepository extends JpaRepository<MetalPrice, Long> {
    Optional<MetalPrice> findByMetalTypeAndStatus(String metalType, boolean status);
}
