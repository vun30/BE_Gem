package online.gemfpt.BE.api;

import ch.qos.logback.core.model.Model;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import online.gemfpt.BE.Service.VNPAYService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")

public class PaymentController {
    @Autowired
    private VNPAYService vnPayService;



    // Chuyển hướng người dùng đến cổng thanh toán VNPAY
    @PostMapping("/submitOrder")
    public String submidOrder(@RequestParam("amount") String orderTotal,
                              HttpServletRequest request) throws Exception {
//        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createUrl(orderTotal);
        return vnpayUrl;
    }


}