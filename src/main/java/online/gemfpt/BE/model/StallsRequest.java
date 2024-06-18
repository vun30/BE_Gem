package online.gemfpt.BE.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data

public class StallsRequest {

    private String stallsName;
    private double revenue;
    private double profit;
    private double countBuyBackOrder;
    private double totalPriceBuyBack;
    private double countOrder;
    private LocalDateTime stallsCreateTime;
    private boolean stallsStatus;
    private List<StaffOnStallsRequest> staffOnStallsList;


}
