package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import online.gemfpt.BE.Service.BuyBackService;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.Service.StallsSellService;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.model.BuyBackProductRequest;
import online.gemfpt.BE.model.ProductsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Test")
@CrossOrigin("*")
public class BuyBackAPI {

    @Autowired
    private StallsSellService stallsSellService;

    @Autowired
    private BuyBackService buyBackService;

    @Autowired
    ProductServices productServices ;


    @PostMapping("/Staff-create-bill-buy-back")
    public ResponseEntity<BillBuyBack > createBillAndProducts(@RequestBody List<BuyBackProductRequest > buyBackProductRequests,
                                                             @RequestParam("customerName") String customerName,
                                                             @RequestParam("customerPhone") String customerPhone) {
        BillBuyBack billBuyBack = buyBackService.createBillAndProducts(customerName, customerPhone, buyBackProductRequests);
        return new ResponseEntity<>(billBuyBack, HttpStatus.CREATED);
    }

        @GetMapping("/+Staff-get-all-bill-buy-back")
    public ResponseEntity<List<BillBuyBack>> getAllBillBuyBacks() {
        List<BillBuyBack> billBuyBacks = buyBackService.getAllBillBuyBacks();
        return new ResponseEntity<>(billBuyBacks, HttpStatus.OK);
    }
//    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
//@PutMapping("/update-product-buyback-to-sell{barcode}")
//public ResponseEntity<?> updateOrCreateProductBuyBack(@PathVariable String barcode, @RequestBody @Valid ProductsRequest productsRequest) {
//    try {
//        Product updatedProduct = productServices.updateAndCreateNewProductBuyBack(barcode, productsRequest);
//        return ResponseEntity.ok(updatedProduct);
//    } catch (EntityNotFoundException e) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found with barcode: " + barcode);
//    } catch (IllegalArgumentException e) {
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//}

}
