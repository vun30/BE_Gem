package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import online.gemfpt.BE.Service.PromotionService;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.model.PromotionRequest.PromotionUpdateRequest;
import online.gemfpt.BE.model.PromotionRequest.PromotionCreateRequest;
import online.gemfpt.BE.model.PromotionRequest.PromotionRequestForBarcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class PromotionAPI {
    @Autowired
    PromotionService promotionService;

    @PostMapping("/api/promotion")
    public ResponseEntity<?> create(@RequestBody @Valid PromotionRequestForBarcode discountRequest){
        try {
            Promotion promotion = promotionService.createDiscount(discountRequest);
            return ResponseEntity.ok(promotion);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/promotion/cate")
    public ResponseEntity<Promotion> createDiscountForCategory(@Valid @RequestBody PromotionCreateRequest discountRequest, @RequestParam TypeEnum category) {
        try {
            Promotion promotion = promotionService.createDiscountForCategory(discountRequest, category);
            return ResponseEntity.ok(promotion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/all-products")
    public ResponseEntity<Promotion> createDiscountForAllProducts(@RequestBody PromotionCreateRequest discountRequest) {
        try {
            Promotion promotion = promotionService.createDiscountForAllProducts(discountRequest);
            return ResponseEntity.ok(promotion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/api/promotion/{id}")
    public ResponseEntity<Promotion> findPromotionById(@RequestParam long promotionId) {
        try {
            Promotion promotion = promotionService.findDiscountByID(promotionId);
            return ResponseEntity.ok(promotion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/api/promotion/{id}")
    public ResponseEntity<Promotion> getDiscountById(@PathVariable Long id,@Valid @RequestBody PromotionUpdateRequest discountRequest){
        Promotion promotion = promotionService.updatePromotion(discountRequest, id);
        if(promotion != null){
            return ResponseEntity.ok(promotion);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/promotion")
    public List<Promotion> getAllDiscounts(){
        return promotionService.getAllDiscount();
    }

    @DeleteMapping("/api/promotion/{disID}")
    public ResponseEntity<Promotion> deleteDiscount(@PathVariable Long disID) {
        try {
            Promotion promotion = promotionService.discountStatus(disID);
            if (promotion != null) {
                return ResponseEntity.ok(promotion);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
