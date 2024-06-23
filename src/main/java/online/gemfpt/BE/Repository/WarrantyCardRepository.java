package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.entity.WarrantyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyCardRepository extends JpaRepository<WarrantyCard,Long> {
    List<WarrantyCard> findByCustomerPhone(int customerPhone);
    List<WarrantyCard> findByBillId(long billId);
}
