package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class ProductAPI {
    @Autowired
    ProductServices productServices;

    @PostMapping("/api/product")
    public ResponseEntity<?> creates (@RequestBody @Valid ProductsRequest productsRequest) {
        try {
            Product product = productServices.creates(productsRequest);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/product")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productServices.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/api/product/{barcode}")
public ResponseEntity<?> updateProduct(@PathVariable String barcode, @RequestBody ProductsRequest productsRequest) {
    if (barcode == null || barcode.trim().isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Barcode cannot be left blank");
    }
    try {
        Product updatedProduct = productServices.updateProduct(barcode, productsRequest);
        return ResponseEntity.ok(updatedProduct);
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found with barcode: " + barcode);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}


    @DeleteMapping("/api/product/{barcode}")
    public ResponseEntity<Product> deleteProduct(@PathVariable String barcode) {
        try {
            Product updatedProduct = productServices.toggleProductActive(barcode);
            if (updatedProduct != null) {
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

     @GetMapping("/api/product/{barcode}")
    public ResponseEntity<Product> findProductByBarcode(@PathVariable String barcode) {
        try {
            Product product = productServices.getProductByBarcode(barcode);
            return ResponseEntity.ok(product);
        } catch (NumberFormatException | EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
