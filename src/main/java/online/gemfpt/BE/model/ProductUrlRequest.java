package online.gemfpt.BE.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Data
@Component
public class ProductUrlRequest {
    @NotBlank(message = "URL cannot be left blank")
    private String urls;
}
