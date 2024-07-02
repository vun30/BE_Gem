package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import online.gemfpt.BE.Service.CustomerService;
import online.gemfpt.BE.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class CustomerAPI {
    @Autowired
    CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestParam String name, @Valid @RequestParam String phone) {
        try {
            Customer createdCustomer = customerService.createCustomer(name,phone);
            return ResponseEntity.ok(createdCustomer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> AllCustomer() {
        List<Customer> customers = customerService.findAll();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customer/{phone}")
    public ResponseEntity<Customer> findCustomerByPhone(@RequestParam String phone) {
        Customer customers = customerService.findCustomerByPhone(phone);
        if(customers != null) {
            return ResponseEntity.ok(customers);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
