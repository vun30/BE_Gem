package online.gemfpt.BE.service;

import online.gemfpt.BE.entity.WarrantyCard;
import online.gemfpt.BE.Repository.WarrantyCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarrantyService {

    @Autowired
    private WarrantyCardRepository warrantyCardRepository;

    public List<WarrantyCard> getWarrantyByCustomerPhone(int customerPhone) {
        return warrantyCardRepository.findByCustomerPhone(customerPhone);
    }

    public List<WarrantyCard> getWarrantyByBillId(long billId) {
        return warrantyCardRepository.findByBillId(billId);
    }
}
