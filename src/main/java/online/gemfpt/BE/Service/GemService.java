package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.GemListRepository;
import online.gemfpt.BE.entity.GemList;
import online.gemfpt.BE.enums.GemStatus;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.model.GemstoneRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GemService {

    @Autowired
    private GemListRepository gemListRepository;

    @Transactional
    public GemList createGemstone(GemstoneRequest request) {
        // Kiểm tra xem barcode đã tồn tại hay chưa
        if (gemListRepository.existsByGemBarcode(request.getGemBarcode())) {
            throw new BadRequestException("Gemstone with barcode " + request.getGemBarcode() + " already exists.");
        }

        GemList gemList = new GemList();
        gemList.setDescription(request.getDescription());
        gemList.setPrice(request.getPrice());
        gemList.setQuantity(1);
        gemList.setCertificateCode(request.getCertificateCode());
        gemList.setUserStatus(GemStatus.NOTUSE);
        gemList.setGemBarcode(request.getGemBarcode());
        gemList.setCarat(request.getCarat());
        gemList.setColor(request.getColor());
        gemList.setClarity(request.getClarity());
        gemList.setCut(request.getCut());
        gemList.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        gemList.setUrl(request.getUrl());

        return gemListRepository.save(gemList);
    }

    public GemList updateGemstone(String gemBarcode, GemStatus userStatus, GemstoneRequest request) {
        Optional<GemList> optionalGemList = gemListRepository.findByGemBarcode(gemBarcode);
        if (optionalGemList.isPresent()) {
            GemList oldGemList = optionalGemList.get();
            if (oldGemList.getUserStatus() == GemStatus.USE || oldGemList.getUserStatus() == GemStatus.FALSE) {
                throw new BadRequestException("Gem đã USE hoặc FALSE không được update");
            }

            if (userStatus == GemStatus.USE || userStatus == GemStatus.FALSE) {
                throw new BadRequestException("Không được đặt trạng thái USE hoặc FALSE cho viên đá này");
            }

            // Cập nhật trạng thái của viên đá quý cũ sang FALSE và thêm tiền tố "UP: " vào barcode của nó
            String updatedBarcode = generateUniqueBarcode(gemBarcode);
            oldGemList.setUserStatus(GemStatus.FALSE);
            String oldBarcode = oldGemList.getGemBarcode();
            oldGemList.setGemBarcode(updatedBarcode + "--: " + gemBarcode);
            gemListRepository.save(oldGemList);

            // Tạo một viên đá quý mới với thông tin cập nhật và giữ nguyên barcode của viên đá quý cũ
            GemList newGemList = new GemList();
            newGemList.setDescription(request.getDescription());
            newGemList.setPrice(request.getPrice());
            newGemList.setQuantity(1);
            newGemList.setCertificateCode(request.getCertificateCode());
            newGemList.setUserStatus(userStatus); // Sử dụng userStatus từ request param
            newGemList.setCarat(request.getCarat());
            newGemList.setColor(request.getColor());
            newGemList.setClarity(request.getClarity());
            newGemList.setCut(request.getCut());
            newGemList.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            newGemList.setBuyRate(request.getBuyRate());
            newGemList.setUrl(request.getUrl());
            newGemList.setGemBarcode(oldBarcode);
            newGemList.setOldGemID(String.valueOf(oldGemList.getGemId()));

            return gemListRepository.save(newGemList);
        } else {
            throw new BadRequestException("Gem không tồn tại");
        }
    }

    public GemList getGemstoneByBarcode(String gemBarcode) {
        return gemListRepository.findByGemBarcode(gemBarcode)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy viên đá quý với barcode đã nhập."));
    }

    public List<GemList> getGemstonesByStatus(GemStatus status) {
        return gemListRepository.findByUserStatus(status);
    }

    // Lấy danh sách tất cả Gemstones
    public List<GemList> getAllGemstones() {
        return gemListRepository.findAll();
    }

    private String generateUniqueBarcode(String existingBarcode) {
        // Example: Add a prefix "UP:" followed by a unique string to mark an update
        String prefix = "UP:";
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniqueString;
    }
}
