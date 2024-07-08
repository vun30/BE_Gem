package online.gemfpt.BE.Service;

import jakarta.persistence.EntityNotFoundException;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.Repository.TypeOfMetalRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Metal;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.entity.TypeOfMetal;
import online.gemfpt.BE.enums.TypeOfProductEnum;
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
    ProductsRepository productsRepository;

    @Autowired
    TypeOfMetalRepository typeOfMetalRepository;

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
            // Kiểm tra trạng thái và số lượng tồn kho
            if (!product.isStatus() || product.getStock() <= 0) {
                continue; // Bỏ qua sản phẩm không thỏa mãn điều kiện
            }

            boolean hasMissingTypeOfMetal = false;
            double totalMetalPrice = 0;
            double totalGemstonePrice = 0;

            // Tính tổng giá của các kim loại
            if (product.getMetals() != null && !product.getMetals().isEmpty()) {
                for (Metal metal : product.getMetals()) {
                    // Thực hiện tính giá bán của kim loại thủ công
                    Optional<TypeOfMetal> typeOfMetal = typeOfMetalRepository.findByMetalType(metal.getName());
                    if (typeOfMetal.isPresent()) {
                        double sellPrice = typeOfMetal.get().getSellPrice();
                        double metalPrice = (metal.getWeight() / 3.75) * sellPrice;
                        totalMetalPrice += metalPrice;
                    } else {
                        hasMissingTypeOfMetal = true;
                        // Log the error with product ID
                        System.out.println("TypeOfMetal not found for product ID: " + product.getProductId() + ", Metal: " + metal.getName());
                        break; // Thoát khỏi vòng lặp kim loại nếu có lỗi
                    }
                }
            }

            // Nếu có lỗi với kim loại, bỏ qua sản phẩm này
            if (hasMissingTypeOfMetal) {
                continue;
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
            product.setPrice(finalPrice); // cập nhật giá sản phẩm

            // Lưu sản phẩm sau khi tính toán giá
            productsRepository.save(product);
        }
    }
//     // Chạy phương thức này mỗi 10 giây
//    @Scheduled(fixedRate = 10000)
//    public void updateProductStatus() {
//        List<Product> productList = productsRepository.findAll();
//
//        for (Product product : productList) {
//            // Kiểm tra số lượng tồn kho của sản phẩm
//            if (product.getStock() == 0) {
//                product.setStatus(false); // Đặt trạng thái sản phẩm thành false nếu hết hàng
//                productsRepository.save(product); // Lưu sản phẩm đã cập nhật trạng thái
//            }
//        }
//    }
}
