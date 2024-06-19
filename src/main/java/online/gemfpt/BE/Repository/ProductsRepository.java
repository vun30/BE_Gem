package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.TypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product,Long> {


    @Query("SELECT p FROM Product p WHERE p.barcode = :barcode OR p.barcode LIKE CONCAT('%|', :barcode)")
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    @Query("SELECT p FROM Product p WHERE (p.barcode = :barcode OR p.barcode LIKE CONCAT('%|', :barcode)) AND p.status = :status")
    Optional<Product> findByBarcodeAndStatus(@Param("barcode") String barcode, @Param("status") boolean status);

    List<Product> findByPriceBetweenAndStatus(double minPrice, double maxPrice, boolean status);

    List<Product> findByStatus(boolean status);


}

