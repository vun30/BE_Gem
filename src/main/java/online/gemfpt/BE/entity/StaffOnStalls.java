package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StaffOnStalls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffWorkingId;

    private String staffName;

    private String staffPhone;

    @Min(1) @Max(2)
    private int workingSlot ;

    private boolean staffWorkingStatus = true  ;

    @ManyToOne
    @JoinColumn(name = "stalls_id")
    private Stalls stalls;
}
