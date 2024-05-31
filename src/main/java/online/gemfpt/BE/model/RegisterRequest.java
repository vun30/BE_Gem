package online.gemfpt.BE.model;

import lombok.Data;

@Data  // sai dc geter va setter cho class nay luon
public class RegisterRequest {
    String phone;
    String password;

}
