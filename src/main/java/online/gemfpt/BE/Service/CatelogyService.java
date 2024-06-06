package online.gemfpt.BE.Service;

import online.gemfpt.BE.entity.Catelogy;
import online.gemfpt.BE.Repository.CatelogyRepository;
import online.gemfpt.BE.model.CatelogyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
