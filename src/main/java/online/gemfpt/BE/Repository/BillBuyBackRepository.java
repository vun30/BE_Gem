package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.BillBuyBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillBuyBackRepository extends JpaRepository<BillBuyBack, Long> {
    Optional<BillBuyBack> findById(Long id);
}