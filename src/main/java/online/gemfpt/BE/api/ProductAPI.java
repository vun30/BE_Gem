package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @PutMapping("/products")
    public ResponseEntity<Product> getProductByBarcode(@RequestBody ProductsRequest productsRequest) {
        Product product = productServices.updateProductByBarcode(productsRequest);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
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


}
