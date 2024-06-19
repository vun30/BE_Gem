package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import online.gemfpt.BE.Service.DiscountService;
import online.gemfpt.BE.entity.Discount;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.model.DiscountUpdateRequest;
import online.gemfpt.BE.model.DiscountCreateRequest;
import online.gemfpt.BE.model.DiscountRequestForBarcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class DiscountAPI {
    @Autowired
    DiscountService discountService;

    @PostMapping("/api/discount")
    public ResponseEntity<?> create(@RequestBody @Valid DiscountRequestForBarcode discountRequest){
        try {
            Discount discount = discountService.createDiscount(discountRequest);
            return ResponseEntity.ok(discount);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/discount/cate")
    public ResponseEntity<Discount> createDiscountForCategory(@Valid @RequestBody DiscountCreateRequest discountRequest, @RequestParam TypeEnum category) {
        try {
            Discount discount = discountService.createDiscountForCategory(discountRequest, category);
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/all-products")
    public ResponseEntity<Discount> createDiscountForAllProducts(@RequestBody DiscountCreateRequest discountRequest) {
        try {
            Discount discount = discountService.createDiscountForAllProducts(discountRequest);
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/api/discount")
    public ResponseEntity<Discount> getDiscountById(@RequestBody DiscountUpdateRequest discountRequest){
        Discount discount = discountService.updateDiscount(discountRequest);
        if(discount != null){
            return ResponseEntity.ok(discount);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/discount")
    public ResponseEntity<List<Discount>> getAllDiscounts(){
        List<Discount> discounts = discountService.getAllDiscount();
        return ResponseEntity.ok(discounts);
    }

    @DeleteMapping("/api/discount/{disID}")
    public ResponseEntity<Discount> deleteDiscount(@PathVariable Long disID) {
        try {
            Discount discount = discountService.discountStatus(disID);
            if (discount != null) {
                return ResponseEntity.ok(discount);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
