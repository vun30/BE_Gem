package online.gemfpt.BE.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class AccountOnStallsRequest {

    private Long stallsWorkingId;


    private boolean staffWorkingStatus ;

    private LocalDateTime startWorkingDateTime;

    private LocalDateTime endWorkingDateTime;


}
