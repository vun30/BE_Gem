package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.BillRepository;
import online.gemfpt.BE.Repository.DiscountRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.Discount;
import online.gemfpt.BE.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DiscountService {
    @Autowired
    BillRepository billRepository;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    AuthenticationRepository accountRepository;


    @Transactional
    public Discount approveDiscountRequest(long discountRequestId, long managerId) {
        Optional<Discount> optionalDiscountRequest = discountRepository.findById(discountRequestId);
        if (optionalDiscountRequest.isPresent()) {
            Discount discountRequest = optionalDiscountRequest.get();
            discountRequest.setApproved(true);

            // Tìm và cập nhật thông tin quản lý phê duyệt
            Optional<Account> optionalManager = accountRepository.findById(managerId);
            if (optionalManager.isPresent()) {
                Account manager = optionalManager.get();
                discountRequest.setManager(manager);
            } else {
                throw new IllegalArgumentException("Invalid manager ID: " + managerId);
            }

            // Cập nhật giảm giá vào hóa đơn
            Bill bill = discountRequest.getBill();
            bill.setDiscount(bill.getDiscount() + discountRequest.getRequestedDiscount());
            billRepository.save(bill);

            return discountRepository.save(discountRequest);
        } else {
            throw new IllegalArgumentException("Invalid discount request ID: " + discountRequestId);
        }
    }

    @Transactional
    public Discount denyDiscountRequest(long discountRequestId, long managerId) {
        Optional<Discount> optionalDiscountRequest = discountRepository.findById(discountRequestId);
        if (optionalDiscountRequest.isPresent()) {
            Discount discountRequest = optionalDiscountRequest.get();
            discountRequest.setApproved(false);

            // Tìm và cập nhật thông tin quản lý từ chối
            Optional<Account> optionalManager = accountRepository.findById(managerId);
            if (optionalManager.isPresent()) {
                Account manager = optionalManager.get();
                discountRequest.setManager(manager);
            } else {
                throw new IllegalArgumentException("Invalid manager ID: " + managerId);
            }

            return discountRepository.save(discountRequest);
        } else {
            throw new IllegalArgumentException("Invalid discount request ID: " + discountRequestId);
        }
    }
}
