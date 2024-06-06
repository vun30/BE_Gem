package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.MetalPriceRepository;
import online.gemfpt.BE.entity.MetalPrice;
import online.gemfpt.BE.entity.TypeOfMetal;
import online.gemfpt.BE.model.MetalPriceRequest;
import online.gemfpt.BE.model.TypeOfMetalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetalPriceService {

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    public MetalPrice createMetalPrice(MetalPriceRequest metalPriceRequest) {
        // Đặt trạng thái của tất cả các MetalPrice cũ thành false trước khi tạo mới
        deactivateAllMetalPrices();

        MetalPrice metalPrice = new MetalPrice();
        metalPrice.setUpdateDate(metalPriceRequest.getUpdateDate());
        metalPrice.setStatus(metalPriceRequest.isStatus());

        List<TypeOfMetal> typeOfMetals = new ArrayList<>();
        for (TypeOfMetalRequest typeOfMetalRequest : metalPriceRequest.getTypeOfMetals()) {
            TypeOfMetal typeOfMetal = new TypeOfMetal();
            typeOfMetal.setMetalType(typeOfMetalRequest.getMetalType());
            typeOfMetal.setSellPrice(typeOfMetalRequest.getSellPrice());
            typeOfMetal.setBuyPrice(typeOfMetalRequest.getBuyPrice());
            typeOfMetal.setMetalPrice(metalPrice);
            typeOfMetals.add(typeOfMetal);
        }

        metalPrice.setTypeOfMetals(typeOfMetals);

        return metalPriceRepository.save(metalPrice);
    }

    private void deactivateAllMetalPrices() {
        List<MetalPrice> metalPrices = getAllMetalPrices();
        for (MetalPrice metalPrice : metalPrices) {
            deleteMetalPrice(metalPrice.getMetalPriceId());
        }
    }

    public MetalPrice updateMetalPrice(Long id, MetalPriceRequest metalPriceRequest) {
        MetalPrice metalPrice = metalPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Metal price not found with id: " + id));

        metalPrice.setUpdateDate(metalPriceRequest.getUpdateDate());
        metalPrice.setStatus(metalPriceRequest.isStatus());

        List<TypeOfMetal> typeOfMetals = new ArrayList<>();
        for (TypeOfMetalRequest typeOfMetalRequest : metalPriceRequest.getTypeOfMetals()) {
            TypeOfMetal typeOfMetal = new TypeOfMetal();
            typeOfMetal.setMetalType(typeOfMetalRequest.getMetalType());
            typeOfMetal.setSellPrice(typeOfMetalRequest.getSellPrice());
            typeOfMetal.setBuyPrice(typeOfMetalRequest.getBuyPrice());
            typeOfMetal.setMetalPrice(metalPrice);
            typeOfMetals.add(typeOfMetal);
        }

        metalPrice.setTypeOfMetals(typeOfMetals);

        return metalPriceRepository.save(metalPrice);
    }

    public List<MetalPrice> getAllMetalPrices() {
        return metalPriceRepository.findAll();
    }

    public MetalPrice getMetalPriceById(Long id) {
        return metalPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Metal price not found with id: " + id));
    }

    public void deleteMetalPrice(Long id) {
        MetalPrice metalPrice = metalPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Metal price not found with id: " + id));
        metalPrice.setStatus(false);
        metalPriceRepository.save(metalPrice);
    }
}
