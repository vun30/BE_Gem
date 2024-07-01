package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findById(Long id);
    List<Bill> findByCustomerPhone(int customerPhone);
}
