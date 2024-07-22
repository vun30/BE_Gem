package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.GemService;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.entity.GemList;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.model.GemstoneRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Gem")
@CrossOrigin("*")
public class GemAPI {

    @Autowired
    private GemService gemService;

    @Autowired
    private ProductServices productServices;

    @PostMapping
    public ResponseEntity<GemList> createGemstone(@RequestBody GemstoneRequest request) {
        return ResponseEntity.ok(gemService.createGemstone(request));
    }

    @PutMapping("/{gemBarcode}")
    public ResponseEntity<GemList> updateGemstone(
            @PathVariable String gemBarcode,
            @RequestParam("userStatus") GemStatus userStatus,
            @RequestBody GemstoneRequest request) {

        return ResponseEntity.ok(gemService.updateGemstone(gemBarcode, userStatus, request));
    }

    @GetMapping("/{gemBarcode}")
    public ResponseEntity<GemList> getGemstoneByBarcode(@PathVariable String gemBarcode) {
        GemList gemList = gemService.getGemstoneByBarcode(gemBarcode);
        return ResponseEntity.ok(gemList);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<GemList>> filterGemstonesByStatus(@RequestParam("status") GemStatus status) {
        List<GemList> gemLists = gemService.getGemstonesByStatus(status);
        return ResponseEntity.ok(gemLists);
    }

    @GetMapping
    public ResponseEntity<List<GemList>> getAllGemstones() {
        List<GemList> gemLists = gemService.getAllGemstones();
        return ResponseEntity.ok(gemLists);
    }
}
