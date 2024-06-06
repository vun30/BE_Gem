package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.MetalPriceRepository;
import online.gemfpt.BE.entity.Metal;
import online.gemfpt.BE.entity.MetalPrice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MetalService {

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    public void setPricePerWeightUnit(Metal metal) {
        Optional<MetalPrice> metalPriceOpt = metalPriceRepository.findByMetalTypeAndStatus(metal.getName(), true);
        if (metalPriceOpt.isPresent()) {
            MetalPrice metalPrice = metalPriceOpt.get();
            metal.setMetalPrice(metalPrice);
            metal.setPricePerWeightUnit();
        } else {
            throw new EntityNotFoundException("Metal price not found for type: " + metal.getName());
        }
    }
}
