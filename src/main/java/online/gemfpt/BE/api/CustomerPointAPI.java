package online.gemfpt.BE.controller;

import online.gemfpt.BE.entity.CustomerPoint;
import online.gemfpt.BE.service.CustomerPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customerPoints")
public class CustomerPointAPI {
    @Autowired
    private CustomerPointService customerPointService;

    @PostMapping
    public ResponseEntity<CustomerPoint> createCustomerPoint(@RequestBody CustomerPoint customerPoint) {
        return ResponseEntity.ok(customerPointService.saveCustomerPoint(customerPoint));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerPoint> getCustomerPointById(@PathVariable long id) {
        CustomerPoint customerPoint = customerPointService.getCustomerPointById(id);
        return customerPoint != null ? ResponseEntity.ok(customerPoint) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CustomerPoint>> getAllCustomerPoints() {
        return ResponseEntity.ok(customerPointService.getAllCustomerPoints());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerPoint(@PathVariable long id) {
        customerPointService.deleteCustomerPoint(id);
        return ResponseEntity.noContent().build();
    }
}
