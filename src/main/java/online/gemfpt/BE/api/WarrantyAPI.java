package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.entity.WarrantyCard;
import online.gemfpt.BE.service.WarrantyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class WarrantyAPI {

    @Autowired
    private WarrantyService warrantyService;

    @GetMapping("/phone/{customerPhone}")
    public List<WarrantyCard> getWarrantyByCustomerPhone(@PathVariable int customerPhone) {
        return warrantyService.getWarrantyByCustomerPhone(customerPhone);
    }

    @GetMapping("/bill/{billId}")
    public List<WarrantyCard> getWarrantyByBillId(@PathVariable long billId) {
        return warrantyService.getWarrantyByBillId(billId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WarrantyCard>> getAllProducts() {
        List<WarrantyCard> warrantyCards = warrantyService.getAllWarranty();
        return ResponseEntity.ok(warrantyCards);
    }
}
