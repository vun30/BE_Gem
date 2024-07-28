package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.UpdateProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpdateProductHistoryRepository extends JpaRepository<UpdateProductHistory, Long> {
     Optional<UpdateProductHistory> findByBarcode(String barcode);

     List<UpdateProductHistory> findByBarcodeOrderByCreateTimeAsc(String barcode);
}
