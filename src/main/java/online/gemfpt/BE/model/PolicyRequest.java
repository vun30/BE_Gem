package online.gemfpt.BE.model;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Data;


@Data
public class PolicyRequest {

     @JsonRawValue
     @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
}
