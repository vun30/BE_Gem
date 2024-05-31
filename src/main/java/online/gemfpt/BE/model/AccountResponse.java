package online.gemfpt.BE.model;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.Entity.Account;
@Getter
@Setter
@Data
public class AccountResponse extends Account {
    String token;
}
