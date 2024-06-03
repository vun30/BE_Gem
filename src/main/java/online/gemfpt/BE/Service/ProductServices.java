package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import online.gemfpt.BE.Entity.Product;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServices {
    @Autowired
    ProductsRepository productsRepository;

    public Product creates(ProductsRequest productsRequest) {
        Optional<Product> existProduct = productsRepository.findByBarcode(productsRequest.getBarcode());
        if (existProduct.isPresent()) {
            throw new IllegalArgumentException("Barcode already exists!");
        }
        Product product = new Product();
        product.setName(productsRequest.getName());
        product.setDescriptions(productsRequest.getDescriptions());
        product.setCategory(productsRequest.getCategory());
        product.setPrice(productsRequest.getPrice());
        product.setPriceRate(productsRequest.getPriceRate());
        product.setStock(productsRequest.getStock());
        product.setUrl(productsRequest.getUrl());
        product.setCreateTime(LocalDateTime.now());
        product.setStatus(true);
        product.setBarcode(productsRequest.getBarcode());

        return productsRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productsRepository.findAll();
    }

    public Product getProductByBarcode(Long barcode) {
        Optional<Product> optionalProduct = productsRepository.findById(barcode);
        return optionalProduct.orElse(null);
    }

    public Product updateProductByBarcode(ProductsRequest productsRequest) {

        Optional<Product> optionalProduct = productsRepository.findByBarcode(productsRequest.getBarcode());
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setDescriptions(productsRequest.getDescriptions().isEmpty() ? product.getDescriptions() : productsRequest.getDescriptions());
            product.setName(productsRequest.getName().isEmpty() ? product.getName() : productsRequest.getName());
            product.setCategory(productsRequest.getCategory().isEmpty() ? product.getCategory() : productsRequest.getCategory());
            product.setPrice(productsRequest.getPrice() == 0 ? product.getPrice() : productsRequest.getPrice());
            product.setPriceRate(productsRequest.getPriceRate() == 0 ? product.getPriceRate() : productsRequest.getPriceRate());
            product.setStock(productsRequest.getStock() == 0 ? product.getStock() : productsRequest.getStock());
            product.setUrl(productsRequest.getUrl().isEmpty() ? product.getUrl() : productsRequest.getUrl());
            product.setBarcode(productsRequest.getBarcode());
            product.setUpdateTime(LocalDateTime.now());

            return productsRepository.save(product);
        } else {
            return null;
        }
    }

    public Product toggleProductActive(String barcode) {
        Product product = productsRepository.findByBarcode(barcode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setStatus(!product.isStatus()); // Đảo ngược trạng thái hiện tại
        return productsRepository.save(product);
    }
}
