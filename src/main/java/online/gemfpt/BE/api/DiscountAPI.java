package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.DiscountService;
import online.gemfpt.BE.entity.Discount;
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

    @PostMapping("/discount/request")
    public ResponseEntity<Discount> sendDiscountRequest(@RequestParam String customerName,
                                                               @RequestParam String customerPhone,
                                                               @RequestParam double requestedDiscount,
                                                               @RequestParam String discountReason) {
        Discount discountRequest = discountService.sendDiscountRequest(customerName, customerPhone, requestedDiscount, discountReason);
        return ResponseEntity.ok(discountRequest);
    }

    @GetMapping("discount")
    public ResponseEntity<List<Discount>> getAllDiscountRequests() {
        List<Discount> requests = discountService.getAll();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("discount/respond")
    public ResponseEntity<Discount> respondToDiscountRequest(@RequestParam long discountRequestId, @RequestParam boolean approved, @RequestParam String managerResponse) {
        Discount discountRequest = discountService.respondToDiscountRequest(discountRequestId, approved, managerResponse);
        return ResponseEntity.ok(discountRequest);
    }

    @GetMapping("discount/{id}")
    public ResponseEntity<Discount> getDiscountById(@RequestParam(required = false) Long id) {
        Discount discount = discountService.getdiscountByID(id);
        return ResponseEntity.ok(discount);
    }
}
