package online.gemfpt.BE.service;

import online.gemfpt.BE.entity.CustomerPoint;
import online.gemfpt.BE.Repository.CustomerPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerPointService {
    @Autowired
    private CustomerPointRepository customerPointRepository;

    public CustomerPoint saveCustomerPoint(CustomerPoint customerPoint) {
        return customerPointRepository.save(customerPoint);
    }

    public CustomerPoint getCustomerPointById(long id) {
        return customerPointRepository.findById(id).orElse(null);
    }

    public List<CustomerPoint> getAllCustomerPoints() {
        return customerPointRepository.findAll();
    }

    public void deleteCustomerPoint(long id) {
        customerPointRepository.deleteById(id);
    }
}
