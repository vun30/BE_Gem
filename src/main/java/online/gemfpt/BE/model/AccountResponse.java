package online.gemfpt.BE.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.entity.Account;
@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountResponse extends Account {
    String token;
}
