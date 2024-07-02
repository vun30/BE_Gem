package online.gemfpt.BE.Service;

import online.gemfpt.BE.Repository.CustomerRepository;
import online.gemfpt.BE.entity.Customer;
import online.gemfpt.BE.entity.Promotion;
import online.gemfpt.BE.entity.PromotionProduct;
import online.gemfpt.BE.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public Customer createCustomer(String name, String phone) {
        Optional<Customer> existingCustomer = customerRepository.findByPhone(phone);
        if (existingCustomer.isPresent()) {
            throw new BadRequestException("Phone number " + phone + " is already in use.");
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setCreateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public Customer findCustomerByPhone(String phone) {
        Optional<Customer> customer = customerRepository.findByPhone(phone);
        if(customer.isPresent()) {
            return customer.get();
        } else {
            throw new BadRequestException("New customer!");
        }
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
