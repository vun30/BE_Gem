package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.StallsSellService;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.StallsSell;
import online.gemfpt.BE.model.AccountOnStallsRequest;
import online.gemfpt.BE.model.StallsSellRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Manager")
@CrossOrigin("*")
public class ManagerAPI {

 @Autowired
    private StallsSellService stallsSellService;
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/create-Stalls")
    public ResponseEntity<StallsSell> createStalls(@RequestBody StallsSellRequest stallsSellRequest) {
        StallsSell stallsSell = stallsSellService.createStalls(stallsSellRequest);
        return ResponseEntity.ok(stallsSell);
    }
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
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
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
      @GetMapping("/all-accounts")
    public ResponseEntity<List <Account>> getAllAccounts() {
        List<Account> accounts = stallsSellService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/active-accounts")
    public ResponseEntity<List<Account>> getAllActiveStaffAccounts() {
        List<Account> accounts = stallsSellService.getAllActiveStaffAccounts();
        return ResponseEntity.ok(accounts);
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
     @GetMapping("/accounts-by-working-datetime")
    public ResponseEntity<List<Account>> getAccountsByWorkingDateTime(
            @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime workingDateTime) {
        List<Account> accounts = stallsSellService.getAccountsByWorkingDate(workingDateTime);
        return ResponseEntity.ok(accounts);
    }


     @GetMapping("/current-account-work-shift")
    public ResponseEntity<Account> getCurrentAccountWorkShift(@AuthenticationPrincipal UserDetails  userDetails) {
        Account account = stallsSellService.getCurrentAccountWorkShift();
        return ResponseEntity.ok(account);
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/accounts-by-working-datetime-and-stalls") // search time for see who working in this date
    public ResponseEntity<List<Account>> getAccountsByWorkingDateTimeAndStallsId(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime workingDateTime,
            @RequestParam Long stallsWorkingId) {
        List<Account> accounts = stallsSellService.getAccountsByWorkingDateAndStallsId(workingDateTime, stallsWorkingId);
        return ResponseEntity.ok(accounts);
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/working-dates/{accountId}")
public ResponseEntity<List<LocalDateTime>> getWorkingDatesByAccountId(@PathVariable Long accountId) {
    List<LocalDateTime> workingDates = stallsSellService.getWorkingDatesByAccountId(accountId);
    return ResponseEntity.ok(workingDates);
}


}
// code phan role
//@PreAuthorize("hasAuthority('MANAGER')")
