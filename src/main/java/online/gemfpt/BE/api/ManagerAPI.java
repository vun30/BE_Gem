package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.StallsSellService;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.StallsSell;
import online.gemfpt.BE.model.AccountOnStallsRequest;
import online.gemfpt.BE.model.StallsSellRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Manager")
@CrossOrigin("*")
public class ManagerAPI {

 @Autowired
    private StallsSellService stallsSellService;

    @PostMapping("/create-Stalls")
    public ResponseEntity<StallsSell> createStalls(@RequestBody StallsSellRequest stallsSellRequest) {
        StallsSell stallsSell = stallsSellService.createStalls(stallsSellRequest);
        return ResponseEntity.ok(stallsSell);
    }

    @PatchMapping("/{accountId}/updateAccount")
    public ResponseEntity<Account> updateAccountOnStalls(
            @PathVariable Long accountId,
            @RequestBody AccountOnStallsRequest accountOnStallsRequest) {
        Account account = stallsSellService.addAccountOnStalls(accountId, accountOnStallsRequest);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

}
