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

import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {
    @Autowired
    BillRepository billRepository;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    AuthenticationRepository accountRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Transactional
    public Discount approveDiscountRequest(long discountRequestId, long managerId) {
        Optional<Discount> optionalDiscountRequest = discountRepository.findById(discountRequestId);
        if (optionalDiscountRequest.isPresent()) {
            Discount discountRequest = optionalDiscountRequest.get();
            discountRequest.setApproved(true);

            // Find and update manager information
            Optional<Account> optionalManager = accountRepository.findById(managerId);
            if (optionalManager.isPresent()) {
                Account manager = optionalManager.get();
                discountRequest.setManager(manager);
            } else {
                throw new IllegalArgumentException("Invalid manager ID: " + managerId);
            }

            // Update discount to the bill
            Bill bill = discountRequest.getBill();
            double discountAmount = bill.getTotalAmount() * (discountRequest.getRequestedDiscount() / 100);
            double updatedTotalAmount = bill.getTotalAmount() - discountAmount;
            bill.setDiscount(discountRequest.getRequestedDiscount());
            bill.setTotalAmount(updatedTotalAmount);
            bill.setStatus(true);

            // Continue bill processing (points, rank, etc.)
            Optional<Customer> optionalCustomer = customerRepository.findByPhone(bill.getCustomerPhone());
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                double customerPoints = updatedTotalAmount / 1000;
                customer.setPoints(customer.getPoints() + customerPoints);

                double memberDiscount = 0;
                if (customer.getPoints() >= 5000000) {
                    customer.setRankCus("Diamond");
                    memberDiscount = 10;
                } else if (customer.getPoints() >= 1000000) {
                    customer.setRankCus("Gold");
                    memberDiscount = 8;
                } else if (customer.getPoints() >= 100000) {
                    customer.setRankCus("Silver");
                    memberDiscount = 5;
                } else {
                    customer.setRankCus("Normal");
                }

                double finalTotalAmount = updatedTotalAmount - (updatedTotalAmount * memberDiscount / 100);
                bill.setVoucher(memberDiscount);
                bill.setTotalAmount(finalTotalAmount);
                customerRepository.save(customer);
            }

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

            // Find and update manager information
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

    public List<Discount> getAll() {
        return discountRepository.findAll();
    }
}
