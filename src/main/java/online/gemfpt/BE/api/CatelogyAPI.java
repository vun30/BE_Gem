package online.gemfpt.BE.api;

import online.gemfpt.BE.entity.Catelogy;
import online.gemfpt.BE.Service.CatelogyService;
import online.gemfpt.BE.model.CatelogyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatelogyAPI {
    @Autowired
    CatelogyService catelogyService;


    @PostMapping("category")
    public ResponseEntity createCatelogy (@RequestBody CatelogyRequest responseRequest) {
        Catelogy catelogy = catelogyService.create(responseRequest);
        return ResponseEntity.ok(catelogy);
    }
}
