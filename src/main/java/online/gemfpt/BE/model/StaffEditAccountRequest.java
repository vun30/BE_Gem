package online.gemfpt.BE.model;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StaffEditAccountRequest {

    private String phone;
    private String description;
    @Column(unique = true)
    private String email;
    private String name;
}
