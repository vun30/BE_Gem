package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findById(Long id);
    List<Bill> findByCustomerPhone(int customerPhone);
    List<Bill> findByStalls(Long stalls);
    List<Bill> findByCashier(String cashier);
    long countByCashier(String cashier);
}
