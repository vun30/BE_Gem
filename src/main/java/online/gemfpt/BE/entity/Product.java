package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.enums.TypeOfProductEnum;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @NotBlank
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updateTime;

    private String descriptions;

    private TypeEnum category;

    private TypeOfProductEnum typeWhenBuyBack;


    private String oldID;

    @Min(0)
    private double price;

    @Min(0)
    private double priceRate;

    private double priceBuyRate;

    @Min(0)
    private double wage;


    private int stock;

    @NotBlank
    @Column(unique = true)
    private String barcode;

    @Transient
    private Double newPrice;

    private boolean status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductUrl> urls;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Gemstone> gemstones;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Metal> metals;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PromotionProduct> promotionProducts;


    @JsonIgnore
    @ManyToOne // many to one buy back bill
    @JoinColumn(name = "billBuyBackProduct_id")
    private BillBuyBack billBuyBack;
}