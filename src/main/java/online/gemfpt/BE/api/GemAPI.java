package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.GemService;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.entity.Gemstone;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.model.GemstoneRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Gem")
@CrossOrigin("*")
public class GemAPI {

    @Autowired
    private GemService  gemService;

    @Autowired
    ProductServices productServices ;

    @PostMapping
    public ResponseEntity<Gemstone> createGemstone(@RequestBody GemstoneRequest request) {
        return ResponseEntity.ok(gemService.createGemstone(request));
    }

    @PutMapping("/{gemBarcode}")
public ResponseEntity<Gemstone> updateGemstone(
        @PathVariable String gemBarcode,
        @RequestParam("userStatus") GemStatus userStatus,
        @RequestBody GemstoneRequest request) {

    return ResponseEntity.ok(gemService.updateGemstone(gemBarcode, userStatus, request));
}


//    @PatchMapping("/{gemBarcode}/status")
//    public ResponseEntity<Gemstone> updateGemstoneStatus(@PathVariable String gemBarcode) {
//        Gemstone updatedGemstone = gemService.updateGemstoneStatus(gemBarcode);
//        return ResponseEntity.ok(updatedGemstone);
//    }

     @GetMapping("/{gemBarcode}")
    public ResponseEntity<Gemstone> getGemstoneByBarcode(@PathVariable String gemBarcode) {
        Gemstone gemstone = gemService.getGemstoneByBarcode(gemBarcode);
        return ResponseEntity.ok(gemstone);
    }

    @GetMapping("/filter")
    public ResponseEntity<List <Gemstone>> filterGemstonesByStatus(@RequestParam("status") GemStatus  status) {
        List<Gemstone> gemstones = gemService.getGemstonesByStatus(status);
        return ResponseEntity.ok(gemstones);
    }

     @GetMapping
    public ResponseEntity<List<Gemstone>> getAllGemstones() {
        List<Gemstone> gemstones = gemService.getAllGemstones();
        return ResponseEntity.ok(gemstones);
    }




}
