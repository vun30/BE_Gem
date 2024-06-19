package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.MetalPriceRepository;
import online.gemfpt.BE.Repository.TypeOfMetalRepository;
import online.gemfpt.BE.entity.MetalPrice;
import online.gemfpt.BE.entity.TypeOfMetal;
import online.gemfpt.BE.model.MetalPriceRequest;
import online.gemfpt.BE.model.TypeOfMetalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MetalPriceService {

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private TypeOfMetalRepository typeOfMetalRepository;

    public MetalPrice createMetalPrice(MetalPriceRequest metalPriceRequest) {
        // Đặt trạng thái của tất cả các MetalPrice cũ thành false trước khi tạo mới
        deactivateAllMetalPrices();
        deactivateAllTypeOfMetal();

        MetalPrice metalPrice = new MetalPrice();

        // Thiết lập updateDate thành ngày giờ hiện tại
        metalPrice.setUpdateDate(LocalDateTime.now());

        // Thiết lập status thành true
        metalPrice.setStatus(true);

        List<TypeOfMetal> typeOfMetals = new ArrayList<>();
        for (TypeOfMetalRequest typeOfMetalRequest : metalPriceRequest.getTypeOfMetals()) {
            TypeOfMetal typeOfMetal = new TypeOfMetal();
            typeOfMetal.setMetalType(typeOfMetalRequest.getMetalType());
            typeOfMetal.setSellPrice(typeOfMetalRequest.getSellPrice());
            typeOfMetal.setBuyPrice(typeOfMetalRequest.getBuyPrice());

            // Thiết lập updateDate của TypeOfMetal từ MetalPrice
            typeOfMetal.setUpdateDate(metalPrice.getUpdateDate());

            typeOfMetal.setMetalPrice(metalPrice);
            typeOfMetals.add(typeOfMetal);
        }

        metalPrice.setTypeOfMetals(typeOfMetals);

        return metalPriceRepository.save(metalPrice);
    }


    private void deactivateAllTypeOfMetal() {
        // Đặt trạng thái của tất cả TypeOfMetal thành false
        List<TypeOfMetal> typeOfMetals = typeOfMetalRepository.findAll();
        for (TypeOfMetal typeOfMetal : typeOfMetals) {
            typeOfMetal.setStatus(false);
            typeOfMetalRepository.save(typeOfMetal);
        }

        // Đặt trạng thái của tất cả MetalPrice thành false
        List<MetalPrice> metalPrices = metalPriceRepository.findAll();
        for (MetalPrice metalPrice : metalPrices) {
            metalPrice.setStatus(false);
            metalPriceRepository.save(metalPrice);
        }
    }
    private void deactivateAllMetalPrices() {
        List<MetalPrice> metalPrices = getAllMetalPrices();
        for (MetalPrice metalPrice : metalPrices) {
            deleteMetalPrice(metalPrice.getMetalPriceId());
        }
    }

//    public MetalPrice updateMetalPrice(String metalType, MetalPriceRequest metalPriceRequest) {
//        TypeOfMetal typeOfMetal = typeOfMetalRepository.findByMetalType(metalType)
//                .orElseThrow(() -> new EntityNotFoundException("Type of metal not found with metalType: " + metalType));
//
//        MetalPrice metalPrice = typeOfMetal.getMetalPrice();
//        if (metalPrice == null) {
//            throw new EntityNotFoundException("Metal price not found for metal type: " + metalType);
//        }
//
//        metalPrice.setUpdateDate(LocalDateTime.now());
//        metalPrice.setStatus(true);
//
//        List<TypeOfMetal> updatedTypeOfMetals = new ArrayList<>();
//        for (TypeOfMetalRequest typeOfMetalRequest : metalPriceRequest.getTypeOfMetals()) {
//            TypeOfMetal updatedTypeOfMetal = new TypeOfMetal();
//            updatedTypeOfMetal.setMetalType(typeOfMetalRequest.getMetalType());
//            updatedTypeOfMetal.setSellPrice(typeOfMetalRequest.getSellPrice());
//            updatedTypeOfMetal.setBuyPrice(typeOfMetalRequest.getBuyPrice());
//            updatedTypeOfMetal.setMetalPrice(metalPrice);
//            updatedTypeOfMetals.add(updatedTypeOfMetal);
//        }
//
//        metalPrice.setTypeOfMetals(updatedTypeOfMetals);
//
//        return metalPriceRepository.save(metalPrice);
//    }


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
