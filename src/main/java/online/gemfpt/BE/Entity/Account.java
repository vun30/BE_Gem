package online.gemfpt.BE.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity // danh dau day la 1 entity
@Getter
@Setter
@ToString
@Data
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column (unique = true)
    String phone ;
    String password;
    String description;
    boolean status = false; // note
    @Column(unique = true)
    private String email;
    int role ;
    String name;
    LocalDateTime createDate;

    public void setCreateDateNow(LocalDateTime createDate) {
        this.createDate = LocalDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.phone;
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // chi tra ve pass luc login

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
