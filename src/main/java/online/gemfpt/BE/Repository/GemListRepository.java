package online.gemfpt.BE.Repository;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.entity.GemList;
import online.gemfpt.BE.entity.Gemstone;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.GemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GemListRepository extends JpaRepository <GemList, Long> {

    Optional<GemList> findByGemBarcode(String gemBarcode);

    List<GemList> findByUserStatus(GemStatus status);



    boolean existsByGemBarcode(String gemBarcode);

     @Query("SELECT COUNT(g) > 0 FROM GemList g WHERE g.gemBarcode = :gemBarcode AND g.userStatus <> :status")
    boolean existsByGemBarcodeAndUserStatusNot(@Param("gemBarcode") String gemBarcode, @Param("status") GemStatus status);

     @Query("SELECT g FROM GemList g WHERE g.gemBarcode IN :barcodes")
    List<GemList> findByBarcodes(@Param("barcodes") List<String> barcodes);

      @Modifying
    @Transactional
    @Query("UPDATE GemList g SET g.userStatus = :status WHERE g.gemBarcode IN :gemBarcodes")
    void updateGemStatusToNotUse(@Param("gemBarcodes") List<String> gemBarcodes, @Param("status") GemStatus status);


}
