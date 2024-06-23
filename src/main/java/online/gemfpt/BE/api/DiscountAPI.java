package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.BillService;
import online.gemfpt.BE.Service.DiscountService;
import online.gemfpt.BE.entity.Discount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class DiscountAPI {

    @Autowired
    DiscountService discountService;

    @PostMapping("/{discountRequestId}/approve")
    public ResponseEntity<Discount> approveDiscountRequest(@PathVariable long discountRequestId, @RequestParam long managerId) {
        Discount discountRequest = discountService.approveDiscountRequest(discountRequestId, managerId);
        return ResponseEntity.ok(discountRequest);
    }

    @PostMapping("/{discountRequestId}/deny")
    public ResponseEntity<Discount> denyDiscountRequest(@PathVariable long discountRequestId, @RequestParam long managerId) {
        Discount discountRequest = discountService.denyDiscountRequest(discountRequestId, managerId);
        return ResponseEntity.ok(discountRequest);
    }
}
