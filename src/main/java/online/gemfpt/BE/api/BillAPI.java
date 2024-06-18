package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.BillService;
import online.gemfpt.BE.entity.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class BillAPI {
    @Autowired
    BillService billService;

    @PostMapping("/api/bill")
    public void addProductToCart(@RequestParam String customerName, @RequestParam int customerPhone, @RequestParam List<String> barcode) {
        billService.addToCart(customerName, customerPhone, barcode);
    }

    @GetMapping("/api/bill/{billId}")
    public ResponseEntity<Bill> getBillDetails(@RequestParam long id) {
        try {
            Bill bill = billService.getBillDetails(id);
            return ResponseEntity.ok(bill);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/api/bill/{customerPhone}")
    public ResponseEntity<List<Bill>> getBillsByCustomerPhone(@PathVariable int customerPhone) {
        List<Bill> bills = billService.getAllBillOfCustumer(customerPhone);
        return ResponseEntity.ok(bills);
    }

    @DeleteMapping("/api/bill/{billId}")
    public ResponseEntity<Void> deleteBill(@RequestParam long billId) {
        try {
            billService.deleteBill(billId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
