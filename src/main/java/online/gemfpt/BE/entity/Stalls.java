package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Stalls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stallsId;

    private String stallsName;

    private LocalDateTime stallsCreateTime;

    private boolean stallsStatus ;

    @OneToMany(mappedBy = "stalls", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffOnStalls> staffOnStallsList;
}
