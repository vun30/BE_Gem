package online.gemfpt.BE.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProductUrlRequest {
    @NotBlank(message = "URL cannot be left blank")
    private String urls;
}
