package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.CustomerService;
import online.gemfpt.BE.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class CustomerAPI {
    @Autowired
    CustomerService customerService;

    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> AllCustomer() {
        List<Customer> customers = customerService.findAll();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customer/{phone}")
    public ResponseEntity<Customer> findCustomerByPhone(@RequestParam int phone) {
        Customer customers = customerService.findCustomerByPhone(phone);
        if(customers != null) {
            return ResponseEntity.ok(customers);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
