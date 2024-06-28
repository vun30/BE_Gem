package online.gemfpt.BE.Service;

import online.gemfpt.BE.Repository.CustomerRepository;
import online.gemfpt.BE.entity.Customer;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import online.gemfpt.BE.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public Customer findCustomerByPhone(int phone) {
        Optional<Customer> customer = customerRepository.findByPhone(phone);
        if(customer.isPresent()) {
            return customer.get();
        } else {
            throw new IllegalArgumentException("New customer!");
        }
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
