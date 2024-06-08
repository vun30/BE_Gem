package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.TypeOfMetalRepository;
import online.gemfpt.BE.entity.Metal;
import online.gemfpt.BE.entity.TypeOfMetal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MetalService {

    @Autowired
    private TypeOfMetalRepository typeOfMetalRepository;

    public void setPricePerWeightUnit(Metal metal) {
        Optional<TypeOfMetal> typeOfMetalOpt = typeOfMetalRepository.findByMetalType(metal.getName());
        if (typeOfMetalOpt.isPresent()) {
            TypeOfMetal typeOfMetal = typeOfMetalOpt.get();
            metal.setTypeOfMetal(typeOfMetal);
            metal.setPricePerWeightUnit();
        } else {
            throw new EntityNotFoundException("Type of metal not found for type: " + metal.getName());
        }
    }
}