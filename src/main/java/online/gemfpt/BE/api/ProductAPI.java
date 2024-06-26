package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class ProductAPI {
    @Autowired
    ProductsRepository productsRepository ;

    @Autowired
    ProductServices productServices;

    @PostMapping("products")
    public ResponseEntity<?> creates (@RequestBody @Valid ProductsRequest productsRequest) {
        try {
            Product product = productServices.creates(productsRequest);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{barcode}")
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

    @PutMapping("/{barcode}")
public ResponseEntity<?> updateOrCreateProduct(@PathVariable String barcode, @RequestBody @Valid ProductsRequest productsRequest) {
    try {
        Product updatedProduct = productServices.updateAndCreateNewProduct(barcode, productsRequest);
        return ResponseEntity.ok(updatedProduct);
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found with barcode: " + barcode);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

//---------search--------------------------///

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productServices.getAllProducts();
        return ResponseEntity.ok(products);
    }

       @GetMapping("/products-true")
    public ResponseEntity<List<Product>> getAllProductsTrue() {
        List<Product> products = productServices.getAllProductsTrue();
        return ResponseEntity.ok(products);
    }



     @GetMapping("/{barcode}")
    public ResponseEntity<?> getProductByBarcode(@PathVariable String barcode) {
        try {
            Product product = productServices.getProductByBarcode(barcode);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam("name") String name) {
        List<Product> products = productServices.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/search/min-max")
public ResponseEntity<List<Product>> searchProductsByPriceRangeAndStatus(
        @RequestParam("minPrice") double minPrice,
        @RequestParam("maxPrice") double maxPrice) {

    List<Product> allProducts = productServices.getAllProducts(); // Lấy tất cả sản phẩm có status true

    // Tạo list mới chỉ chứa các sản phẩm có giá nằm trong khoảng minPrice đến maxPrice
    List<Product> productsInPriceRange = allProducts.stream()
            .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
            .collect(Collectors .toList());

    // Trả về danh sách sản phẩm nằm trong khoảng giá minPrice đến maxPrice cho client
    return ResponseEntity.ok(productsInPriceRange);
}

    @GetMapping("/search/gemstone")
    public ResponseEntity<List<Product>> searchProductsByGemstoneAttributes(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String clarity,
            @RequestParam(required = false) String cut,
            @RequestParam(required = false) Double carat) {

        List<Product> products = productServices.searchProductsByGemstoneAttributes(color, clarity, cut, carat);
        return ResponseEntity.ok(products);
    }

     // Tìm kiếm sản phẩm theo metal type
    @GetMapping("/search/metaltype")
    public ResponseEntity<List<Product>> searchProductsByMetalType(@RequestParam String metalType) {
        List<Product> products = productServices.searchProductsByMetalType(metalType);
        return ResponseEntity.ok(products);
    }


}
