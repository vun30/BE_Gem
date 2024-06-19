package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.DiscountProductRepository;
import online.gemfpt.BE.Repository.DiscountRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.Discount;
import online.gemfpt.BE.entity.DiscountProduct;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.model.DiscountUpdateRequest;
import online.gemfpt.BE.model.DiscountCreateRequest;
import online.gemfpt.BE.model.DiscountRequestForBarcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@Service
public class DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private ProductsRepository productRepository;

    @Autowired
    private DiscountProductRepository discountProductRepository;

    public List<Discount> getAllDiscount(){
        return discountRepository.findAll();
    }

    public Discount findDiscountByID(Long disID){
        Optional<Discount> discount = discountRepository.findById(disID);
        return discount.orElse(null);
    }

    public Discount createDiscount(DiscountRequestForBarcode discountRequest){
        List <Product> productList = new ArrayList<>();
        for (String barcode : discountRequest.getBarcode()) {
            Optional<Product> product = productRepository.findByBarcode(barcode);
            if (product.isEmpty()) {
                throw new IllegalArgumentException("Barcode don't exists!");
            }
            productList.add(product.get());
        }

        Discount discount = new Discount();
        discount.setProgramName(discountRequest.getProgramName());
        discount.setDiscountRate(discountRequest.getDiscountRate());
        discount.setDescription(discountRequest.getDescription());
        discount.setApplicableProducts(discountRequest.getApplicableProducts());
        discount.setStartTime(LocalDateTime.now());
        discount.setEndTime(discountRequest.getEndTime());
        discount.setStatus(true);

        discountRepository.save(discount);

        for (Product product : productList) {
            DiscountProduct discountProduct = new DiscountProduct();
            discountProduct.setProduct(product);
            discountProduct.setDiscount(discount);
            discountProduct.setDiscountValue(discountRequest.getDiscountRate());
            discountProduct.setActive(true);

            discountProductRepository.save(discountProduct);
        }

        return discount;
    }

    public Discount createDiscountForCategory(DiscountCreateRequest discountRequest, TypeEnum category){
        List<Product> productList = productRepository.findByCategory(category);
        if (productList.isEmpty()) {
            throw new IllegalArgumentException("No products found in the specified category.");
        }

        Discount discount = new Discount();

        discount.setProgramName(discountRequest.getProgramName());
        discount.setDiscountRate(discountRequest.getDiscountRate());
        discount.setDescription(discountRequest.getDescription());
        discount.setApplicableProducts(String.valueOf(category));
        discount.setStartTime(LocalDateTime.now());
        discount.setEndTime(discountRequest.getEndTime());
        discount.setStatus(true);

        discountRepository.save(discount);

        for (Product product : productList) {
            DiscountProduct discountProduct = new DiscountProduct();
            discountProduct.setProduct(product);
            discountProduct.setDiscount(discount);
            discountProduct.setDiscountValue(discountRequest.getDiscountRate());
            discountProduct.setActive(true);

            discountProductRepository.save(discountProduct);
        }

        return discount;
    }

    public Discount createDiscountForAllProducts(DiscountCreateRequest discountRequest){
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new IllegalArgumentException("No products found in the inventory.");
        }

        Discount discount = new Discount();
        discount.setProgramName(discountRequest.getProgramName());
        discount.setDiscountRate(discountRequest.getDiscountRate());
        discount.setDescription(discountRequest.getDescription());
        discount.setApplicableProducts("All Products");
        discount.setStartTime(LocalDateTime.now());
        discount.setEndTime(discountRequest.getEndTime());
        discount.setStatus(true);

        discountRepository.save(discount);

        for (Product product : productList) {
            DiscountProduct discountProduct = new DiscountProduct();
            discountProduct.setProduct(product);
            discountProduct.setDiscount(discount);
            discountProduct.setDiscountValue(discountRequest.getDiscountRate());
            discountProduct.setActive(true);

            discountProductRepository.save(discountProduct);
        }

        return discount;
    }

    public Discount updateDiscount(DiscountUpdateRequest discountRequest){
        Optional<Discount> discountExist = discountRepository.findById(discountRequest.getDisID());
        if(discountExist.isPresent()){
            Discount discount = discountExist.get();
            discount.setProgramName(discountRequest.getProgramName().isEmpty() ? discount.getProgramName() : discountRequest.getProgramName());
            discount.setDiscountRate(discountRequest.getDiscountRate() == 0 ? discount.getDiscountRate() : discountRequest.getDiscountRate());
            discount.setDescription(discountRequest.getDescription().isEmpty() ? discount.getDescription() : discountRequest.getDescription());
            discount.setEndTime(LocalDateTime.now());

            return discountRepository.save(discount);
        }else{
            return null;
        }
    }

    public Discount discountStatus(Long disID){
        Discount discount = discountRepository.findById(disID).orElseThrow(() -> new EntityNotFoundException("Discount not found"));
        discount.setStatus(!discount.isStatus());
        return discountRepository.save(discount);
    }


}
