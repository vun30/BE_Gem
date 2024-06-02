package online.gemfpt.BE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetail {
    private String recipient;
    private String msgBody;
    private String subject;
    private String fullname;
    private String buttonValue;
    private String attachment;
    private String link;
}
