package online.gemfpt.BE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.gemfpt.BE.entity.Bill;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Bill bill;
    private double customerChange;
}
