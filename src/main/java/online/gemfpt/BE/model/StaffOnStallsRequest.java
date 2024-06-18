package online.gemfpt.BE.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StaffOnStallsRequest {

    private String staffName;
    private String staffPhone;

    @Min(1) @Max(2)
    private int workingSlot;
    private boolean staffWorkingStatus;


}
