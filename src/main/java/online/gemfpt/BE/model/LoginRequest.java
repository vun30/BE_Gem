package online.gemfpt.BE.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.RoleEnum;

@Getter
@Setter
@Data  // sai dc geter va setter cho class nay luon
public class LoginRequest {
    String email;
    String password;




}
