package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class CartAPI {
    @Autowired
    CartService cartService;

    @PostMapping("/add")
    public void addProductToCart(@RequestParam String customerName, @RequestParam int customerPhone, @RequestParam List<String> barcode) {
        cartService.addToCart(customerName, customerPhone, barcode);
    }
}
