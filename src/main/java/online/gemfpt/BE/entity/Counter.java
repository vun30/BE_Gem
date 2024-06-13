package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
public class Counter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String counterName;
    private double revenue;

    @OneToMany(mappedBy = "counter", cascade = CascadeType.ALL)
    private List<Account> employees;
}