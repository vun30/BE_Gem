package online.gemfpt.BE.Repository;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.entity.Gemstone;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.GemStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GemstoneRepository extends JpaRepository<Gemstone, Long> {
//     @EntityGraph(attributePaths = "gemProperties")
//     List<Gemstone> findByProduct(Product product);
//     List<Gemstone> findByColorAndClarityAndCutAndCarat(String color, String clarity, String cut, Double carat);
     Optional <Gemstone> findByGemBarcode(String gemBarcode);
     List<Gemstone> findByUserStatus(GemStatus status);
      @Query("SELECT g FROM Gemstone g WHERE g.gemBarcode IN :barcodes")
    List<Gemstone> findByBarcodes(@Param("barcodes") List<String> barcodes);

     @Modifying
@Transactional
@Query("UPDATE Gemstone g SET g.product = null, g.userStatus = :status WHERE g.product.productId = :productId")
void detachFromProductById(@Param("productId") Long productId, @Param("status") GemStatus status);


    @Query("SELECT COUNT(g) > 0 FROM Gemstone g WHERE g.gemBarcode = :gemBarcode AND g.userStatus <> :status")
    boolean existsByGemBarcodeAndUserStatusNot(@Param("gemBarcode") String gemBarcode, @Param("status") GemStatus status);


    @Query("SELECT g FROM Gemstone g WHERE g.product IS NULL AND g.userStatus = :status")
    List<Gemstone> findGemstonesWithNullProductAndUserStatusUse(@Param("status") GemStatus status);

  @Modifying
@Transactional
@Query("UPDATE Gemstone g SET g.product = null, g.userStatus = :newStatus WHERE g.product.productId = :productId AND g.userStatus = :status")
void detachGemsByProductId(
        @Param("productId") Long productId,
        @Param("status") GemStatus status,
        @Param("newStatus") GemStatus newStatus);

  boolean existsByGemBarcode(String gemBarcode);

  @Query("SELECT g FROM Gemstone g WHERE g.gemBarcode IN :barcodes AND g.userStatus <> :status")
    List<Gemstone> findInvalidOrUsedGemstones(@Param("barcodes") List<String> barcodes, @Param("status") GemStatus status);

    @Modifying
    @Query("UPDATE Gemstone g SET g.product.id = :productId, g.userStatus = :status WHERE g.gemBarcode IN :gemBarcodes")
    void updateGemstones(@Param("productId") Long productId, @Param("gemBarcodes") List<String> gemBarcodes, @Param("status") GemStatus status);


}




