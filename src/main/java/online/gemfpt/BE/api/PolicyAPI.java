package online.gemfpt.BE.api;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.BuyBackService;
import online.gemfpt.BE.Service.PolicyService;
import online.gemfpt.BE.Service.ProductServices;
import online.gemfpt.BE.Service.StallsSellService;
import online.gemfpt.BE.entity.*;
import online.gemfpt.BE.enums.TypeOfProductEnum;
import online.gemfpt.BE.model.AccountOnStallsRequest;
import online.gemfpt.BE.model.BuyBackProductRequest;
import online.gemfpt.BE.model.PolicyRequest;
import online.gemfpt.BE.model.StallsSellRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/Policy")
@CrossOrigin("*")
public class PolicyAPI {

    @Autowired
    private PolicyService  policyService;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<Policy> addPolicy(@RequestBody PolicyRequest policyRequest) {
        Policy newPolicy = policyService.addPolicy(policyRequest);
        return ResponseEntity.ok(newPolicy);
    }

    @GetMapping
    public ResponseEntity<List<Policy>> viewAllPolicies() {
        List<Policy> policies = policyService.viewAllPolicies();
        return ResponseEntity.ok(policies);
    }


    @GetMapping("/active")
    public ResponseEntity<Policy> viewActivePolicy() {
        Policy activePolicy = policyService.viewActivePolicy();
        if (activePolicy == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activePolicy);
    }
}
