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
public class CartAPI {
    @Autowired
    BillService billService;

    @PostMapping("/api/cart")
    public void addProductToCart(@RequestParam String customerName, @RequestParam int customerPhone, @RequestParam List<String> barcode) {
        billService.addToCart(customerName, customerPhone, barcode);
    }

//    @GetMapping("/api/cart")
//    public ResponseEntity<List<Bill>> CardDetails(@RequestParam long cartId){
//        List<Bill> carts = billService.getBillDetails(cartId);
//        return ResponseEntity.ok(carts);
//    }
}
