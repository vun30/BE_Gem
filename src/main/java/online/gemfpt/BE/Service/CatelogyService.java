package online.gemfpt.BE.Service;

import lombok.Setter;
import online.gemfpt.BE.Entity.Account;
import online.gemfpt.BE.Entity.Catelogy;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.CatelogyRepository;
import online.gemfpt.BE.model.CatelogyRequest;
import online.gemfpt.BE.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatelogyService {
    @Autowired
    CatelogyRepository CatelogyRepository;

    public Catelogy create /* sreate category*/ (CatelogyRequest catelogyRequest) {
        Catelogy catelogy = new Catelogy();
        catelogy.setName(catelogyRequest.getName());
        catelogy.setPrice(catelogyRequest.getPrice());

        return CatelogyRepository.save(catelogy);
    }

}
