package online.gemfpt.BE.model;

import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.TypeMoneyChange;

@Getter
@Setter
public class MoneyChangeRequest {
    private Long stallsSellId;
    private double amount;
    private Long billId;
    private TypeMoneyChange TypeChange;
}
