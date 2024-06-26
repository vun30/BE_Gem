package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.Repository.TypeOfMetalRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Metal;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.entity.TypeOfMetal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledTasks {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    ProductsRepository productsRepository ;

    @Autowired
    TypeOfMetalRepository typeOfMetalRepository  ;

    @Autowired
    MetalService metalService;

    // Phương thức này sẽ chạy mỗi phút
    @Scheduled(fixedRate = 60000)
    public void updateStaffWorkingStatus() {
        // Lấy danh sách tất cả các tài khoản
        List<Account> accounts = authenticationRepository.findAll();

        // Kiểm tra và cập nhật staffWorkingStatus cho từng tài khoản
        for (Account account : accounts) {
            if (account.getEndWorkingDateTime() != null && LocalDateTime.now().isAfter(account.getEndWorkingDateTime())) {
                account.setStaffWorkingStatus(false);
                authenticationRepository.save(account);
            }
        }
    }
      @Scheduled(fixedRate = 10000) // 10s
    public void updateProductPricesByTypeOfMetal() {
        List<Product> productList = productsRepository.findAll();

        for (Product product : productList) {
            boolean hasMissingTypeOfMetal = false;
            double totalMetalPrice = 0;
            double totalGemstonePrice = 0;

            // Tính tổng giá của các kim loại
            if (product.getMetals() != null && !product.getMetals().isEmpty()) {
                for (Metal metal : product.getMetals()) {
                    try {
                        metalService.setPricePerWeightUnit(metal); // Sử dụng service để set giá
                        totalMetalPrice += metal.getWeight() * metal.getPricePerWeightUnit();
                    } catch (EntityNotFoundException e) {
                        hasMissingTypeOfMetal = true;
                        // Log the error with product ID
                        System.out.println("TypeOfMetal not found for product ID: " + product.getProductId() + ", Metal: " + metal.getName());
                    }
                }
            }

            // Tính tổng giá của các đá quý
            if (product.getGemstones() != null) {
                totalGemstonePrice = product.getGemstones().stream()
                        .mapToDouble(gemstone -> gemstone.getPrice() * gemstone.getQuantity())
                        .sum();
            }

            // Tính giá cuối cùng của sản phẩm
            double totalPrice = totalMetalPrice + totalGemstonePrice;
            double finalPrice = totalPrice + (totalPrice * product.getPriceRate() / 100);
            product.setPrice(finalPrice);

            // Lưu sản phẩm nếu không có kim loại nào bị thiếu
            if (!hasMissingTypeOfMetal) {
                productsRepository.save(product);
            }
        }
    }


}
