package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.MetalPriceService;
import online.gemfpt.BE.entity.MetalPrice;
import online.gemfpt.BE.model.MetalPriceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/metalprices")
@CrossOrigin("*")
public class MetalPriceAPI {

    @Autowired
    private MetalPriceService metalPriceService;

    @PostMapping
    public ResponseEntity<MetalPrice> createMetalPrice(@RequestBody MetalPriceRequest metalPriceRequest) {
        MetalPrice createdMetalPrice = metalPriceService.createMetalPrice(metalPriceRequest);
        return ResponseEntity.ok(createdMetalPrice);
    }

//    @PutMapping("/{metalType}")
//    public ResponseEntity<MetalPrice> updateMetalPrice(@PathVariable String metalType, @RequestBody MetalPriceRequest metalPriceRequest) {
//        MetalPrice updatedMetalPrice = metalPriceService.updateMetalPrice(metalType, metalPriceRequest);
//        return ResponseEntity.ok(updatedMetalPrice);
//    }

    @GetMapping
    public ResponseEntity<List<MetalPrice>> getAllMetalPrices() {
        List<MetalPrice> metalPrices = metalPriceService.getAllMetalPrices();
        return ResponseEntity.ok(metalPrices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetalPrice> getMetalPriceById(@PathVariable Long id) {
        MetalPrice metalPrice = metalPriceService.getMetalPriceById(id);
        return ResponseEntity.ok(metalPrice);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteMetalPrice(@PathVariable Long id) {
//        metalPriceService.deleteMetalPrice(id);
//        return ResponseEntity.noContent().build();
//    }
}
