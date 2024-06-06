package online.gemfpt.BE.model;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MetalPriceRequest {


    @NotNull(message = "Update date cannot be null")
    private LocalDateTime updateDate;

    private boolean status;

    private List<TypeOfMetalRequest> typeOfMetals;
}
