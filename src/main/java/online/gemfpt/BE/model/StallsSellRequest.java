package online.gemfpt.BE.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class StallsSellRequest {
    private String stallsSellName;
    private LocalDateTime stallsSellCreateTime;
    private boolean stallsSellStatus;
}
