package online.gemfpt.BE.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ResetPasswordRequest {
    private String password;

}
