package online.gemfpt.BE.Service;

import online.gemfpt.BE.Repository.UpdateProductHistoryRepository;
import online.gemfpt.BE.entity.UpdateProductHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateProductHistoryService {

    @Autowired
    private UpdateProductHistoryRepository  updateProductHistoryRepository;

    public List<UpdateProductHistory> getAllHistory() {
        return updateProductHistoryRepository.findAll();
    }

//    public Optional<UpdateProductHistory> getHistoryByBarcode(String barcode) {
//        return updateProductHistoryRepository.findByBarcode(barcode);
//    }
}
