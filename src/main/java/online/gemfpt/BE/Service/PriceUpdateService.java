package online.gemfpt.BE.Service;

import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.Service.MetalService;
import online.gemfpt.BE.entity.Gemstone;
import online.gemfpt.BE.entity.Metal;
import online.gemfpt.BE.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceUpdateService {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MetalService metalService;

//    @Scheduled(fixedRate = 6000) // 60000 milliseconds = 1 minute
//    public void updateProductPrices() {
//        List<Product> products = productsRepository.findAll();
//
//        for (Product product : products) {
//            double totalMetalPrice = 0;
//            if (product.getMetals() != null) {
//                for (Metal  metal : product.getMetals()) {
//                    metalService.setPricePerWeightUnit(metal); // Cập nhật giá kim loại
//                    double metalPrice = metal.getPricePerWeightUnit();
//                    totalMetalPrice += metalPrice;
//                }
//            }
//
//            double totalGemstonePrice = 0;
//            if (product.getGemstones() != null) {
//                for (Gemstone gemstone : product.getGemstones()) {
//                    double gemstonePrice = gemstone.getPrice() * gemstone.getQuantity();
//                    totalGemstonePrice += gemstonePrice;
//                }
//            }
//
//            double totalPrice = totalMetalPrice + totalGemstonePrice;
//            double totalPriceXRate = totalPrice * product.getPriceRate();
//            double totalPrice2 = totalPrice + totalPriceXRate;
//            product.setPrice(totalPrice2);
//
//            productsRepository.save(product);
//        }
//    }
}
