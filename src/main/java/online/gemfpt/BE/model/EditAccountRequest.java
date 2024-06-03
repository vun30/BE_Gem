package online.gemfpt.BE.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditAccountRequest {
    private String email;
    private String name;
    private String phone;
    private String description;
}
