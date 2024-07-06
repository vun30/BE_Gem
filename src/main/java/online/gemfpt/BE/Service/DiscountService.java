package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.BillRepository;
import online.gemfpt.BE.Repository.CustomerRepository;
import online.gemfpt.BE.Repository.DiscountRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.Customer;
import online.gemfpt.BE.entity.Discount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Discount sendDiscountRequest(String customerName, String customerPhone, double requestedDiscount, String discountReason) {
        Optional<Customer> optionalCustomer = customerRepository.findByPhone(customerPhone);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            Discount discountRequest = new Discount();
            discountRequest.setRequestedDiscount(requestedDiscount);
            discountRequest.setDiscountReason(discountReason);
            discountRequest.setApproved(false);
            discountRequest.setRequestTime(LocalDateTime.now());
            discountRequest.setCustomer(customer);
            return discountRepository.save(discountRequest);
        } else {
            throw new IllegalArgumentException("Customer not found with phone: " + customerPhone);
        }
    }

    @Transactional
    public Discount respondToDiscountRequest(long discountRequestId, boolean approved, String managerResponse) {
        Optional<Discount> optionalRequest = discountRepository.findById(discountRequestId);
        if (optionalRequest.isPresent()) {
            Discount discountRequest = optionalRequest.get();
            discountRequest.setApproved(approved);
            discountRequest.setManagerResponse(managerResponse);
            discountRequest.setResponseTime(LocalDateTime.now());
            discountRequest.setStatusUse(false);  // not  use
            return discountRepository.save(discountRequest);
        } else {
            throw new IllegalArgumentException("Discount request not found with id: " + discountRequestId);
        }
    }

    public List<Discount> getAll() {
        return discountRepository.findAll();
    }
}
