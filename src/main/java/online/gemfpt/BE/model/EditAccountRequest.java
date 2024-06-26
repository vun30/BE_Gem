package online.gemfpt.BE.model;

import jakarta.persistence.Column;
import lombok.Data;
import online.gemfpt.BE.enums.RoleEnum;

@Data
public class EditAccountRequest {

    private String phone;

    private String description;

    private boolean status;


    private RoleEnum role;

    private String name;
}
