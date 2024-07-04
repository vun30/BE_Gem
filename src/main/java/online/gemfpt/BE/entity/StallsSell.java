package online.gemfpt.BE.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class StallsSell {
    @Id
    @GeneratedValue (strategy = GenerationType .IDENTITY)
    private Long stallsSellId;

    private String stallsSellName;

    private LocalDateTime stallsSellCreateTime;

    private boolean stallsSellStatus;

    @Min(0)
    private double money;
}
