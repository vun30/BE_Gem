package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.GemStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Gemstone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long gemId;

    String description;

    String color;

    String clarity;

    String cut;

    double carat;

    String oldGemID;

    String url ;

    @Column(unique = true)
    @NotNull(message = "gem Barcode cannot be null")
    String gemBarcode;

    @NotNull
    double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updateTime;


    GemStatus userStatus;

    @Min(0)
    double buyRate ; //  when buy back => price = price - ( price x priceRate )

    String certificateCode;

    @Min(0)
    int quantity  ;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "productId")
    private Product product;



}