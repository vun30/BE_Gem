package online.gemfpt.BE.api;

import online.gemfpt.BE.Service.StallsSellService;
import online.gemfpt.BE.entity.MoneyChangeHistory;
import online.gemfpt.BE.enums.TypeMoneyChange;
import online.gemfpt.BE.model.MoneyChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/MoneyChange")
@CrossOrigin("*")
public class MoneymanagementAPI {

    @Autowired
    StallsSellService stallsSellService ;


   @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/change-money")
    public ResponseEntity<MoneyChangeHistory> changeMoneyInStalls(
            @RequestBody MoneyChangeRequest  moneyChangeRequest,
            @RequestParam TypeMoneyChange  typeChange) {
        MoneyChangeHistory moneyChangeHistory = stallsSellService.changeMoneyInStalls(moneyChangeRequest, typeChange);
        return ResponseEntity.ok(moneyChangeHistory);
    }


    @GetMapping("/{stallsSellId}/change-history")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<List<MoneyChangeHistory>> getStallsChangeHistory(
            @PathVariable Long stallsSellId) {
        List<MoneyChangeHistory> changeHistory = stallsSellService.getMoneyChangeHistory(stallsSellId);
        return new ResponseEntity<>(changeHistory, HttpStatus.OK);
    }
}
