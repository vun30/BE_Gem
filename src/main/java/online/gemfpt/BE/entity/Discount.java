package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double requestedDiscount;
    private String discountReason;
    private boolean isApproved;
    private LocalDateTime requestTime;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Account manager; // Người quản lý phê duyệt
}
