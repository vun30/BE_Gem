package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.GemStatus;

@Getter
@Setter
@Data
public class GemstoneRequest {

    private String description;

    @Min(value = 0, message = "Price must be  >= 0")
    private double price;

    @Min(value = 0, message = "Quantity must be >=  0")
    private int quantity ;

    @NotBlank(message = "Certificate Code cannot be left blank")
    private String certificateCode;


      GemStatus userStatus;

       String gemBarcode;

       String url ;

    private double carat;

    private String color;

    private String clarity;

    private String cut;


    @Min(0)
    double buyRate ; //  when buy back => price = price - ( price x priceRate )


}
