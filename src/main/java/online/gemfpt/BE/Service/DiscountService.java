package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.DiscountRepository;
import online.gemfpt.BE.entity.Discount;
import online.gemfpt.BE.model.DiscountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@Service
public class DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    public List<Discount> getAllDiscount(){
        return discountRepository.findAll();
    }

    public Discount findDiscountByID(Long disID){
        Optional<Discount> discount = discountRepository.findById(disID);
        return discount.orElse(null);
    }

    public Discount createDiscount(DiscountRequest discountRequest){
        Optional<Discount> existID = discountRepository.findById(discountRequest.getDisID());
        if (existID.isPresent()) {
            throw new IllegalArgumentException("Discount id already exists!");
        }
        Discount discount = new Discount();
        discount.setDisID(discountRequest.getDisID());
        discount.setProgramName(discountRequest.getProgramName());
        discount.setDiscountRate(discountRequest.getDiscountRate());
        discount.setDescription(discountRequest.getDescription());
        discount.setStartTime(LocalDateTime.now());
        discount.setStatus(true);

        return discountRepository.save(discount);
    }

    public Discount updateDiscount(DiscountRequest discountRequest){
        Optional<Discount> discountExist = discountRepository.findById(discountRequest.getDisID());
        if(discountExist.isPresent()){
            Discount discount = discountExist.get();
            discount.setDisID(discountRequest.getDisID() == 0 ? discount.getDisID() : discountRequest.getDisID());
            discount.setProgramName(discountRequest.getProgramName().isEmpty() ? discount.getProgramName() : discountRequest.getProgramName());
            discount.setDiscountRate(discountRequest.getDiscountRate() == 0 ? discount.getDiscountRate() : discountRequest.getDiscountRate());
            discount.setDescription(discountRequest.getDescription().isEmpty() ? discount.getDescription() : discountRequest.getDescription());
            discount.setEndTime(LocalDateTime.now());

            return discountRepository.save(discount);
        }else{
            return null;
        }
    }

    public Discount discountStatus(Long disID){
        Discount discount = discountRepository.findById(disID).orElseThrow(() -> new EntityNotFoundException("Discount not found"));
        discount.setStatus(!discount.isStatus());
        return discountRepository.save(discount);
    }
}
