package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class ProductAPI {
    @Autowired
    ProductServices productServices;

    @PostMapping("create-products")
    public ResponseEntity<?> creates (@RequestBody @Valid ProductsRequest productsRequest) {
        try {
            Product product = productServices.creates(productsRequest);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/products/{barcode}")
public ResponseEntity<Product> updateProductByBarcode(@PathVariable String barcode, @RequestBody ProductsRequest productsRequest) {
    try {
        Long barcodeLong = Long.parseLong(barcode); // Chuyển đổi barcode từ String sang Long
        Product updatedProduct = productServices.updateProductByBarcode(barcode, productsRequest);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (NumberFormatException e) {
        return ResponseEntity.badRequest().build();
    }
}

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productServices.getAllProducts();
        return ResponseEntity.ok(products);
    }
    @DeleteMapping("/delete-product/{barcode}")
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

    @GetMapping("/products/{barcode}")
    public ResponseEntity<Product> findProductByBarcode(@PathVariable String barcode) {
        try {
            Long barcodeLong = Long.parseLong(barcode);
            Product product = productServices.getProductByBarcode(barcodeLong);
            return ResponseEntity.ok(product);
        } catch (NumberFormatException | EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
