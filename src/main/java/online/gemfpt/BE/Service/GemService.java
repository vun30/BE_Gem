package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.GemstoneRepository;
import online.gemfpt.BE.entity.Gemstone;
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
    private GemstoneRepository gemstoneRepository;

   @Transactional
public Gemstone createGemstone(GemstoneRequest request) {
    // Kiểm tra xem barcode đã tồn tại hay chưa
    if (gemstoneRepository.existsByGemBarcode(request.getGemBarcode())) {
        throw new BadRequestException("Gemstone with barcode " + request.getGemBarcode() + " already exists.");
    }

    Gemstone gemstone = new Gemstone();
    gemstone.setDescription(request.getDescription());
    gemstone.setPrice(request.getPrice());
    gemstone.setQuantity(1);
    gemstone.setCertificateCode(request.getCertificateCode());
    gemstone.setUserStatus(GemStatus.NOTUSE);
    gemstone.setGemBarcode(request.getGemBarcode());
    gemstone.setCarat(request.getCarat());
    gemstone.setColor(request.getColor());
    gemstone.setClarity(request.getClarity());
    gemstone.setCut(request.getCut());
    gemstone.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    gemstone.setUrl(request.getUrl());

    return gemstoneRepository.save(gemstone);
}
    public Gemstone updateGemstone(String gemBarcode, GemStatus userStatus, GemstoneRequest request) {
    Optional<Gemstone> optionalGemstone = gemstoneRepository.findByGemBarcode(gemBarcode);
    if (optionalGemstone.isPresent()) {
        Gemstone oldGemstone = optionalGemstone.get();
        if (oldGemstone.getUserStatus() == GemStatus.USE || oldGemstone.getUserStatus() == GemStatus.FALSE) {
            throw new BadRequestException("Gem đã USE hoặc FALSE không được update");
        }

        if (userStatus == GemStatus.USE || userStatus == GemStatus.FALSE) {
            throw new BadRequestException("Không được đặt trạng thái USE hoặc FALSE cho viên đá này");
        }

        // Cập nhật trạng thái của viên đá quý cũ sang FALSE và thêm tiền tố "UP: " vào barcode của nó
        String updatedBarcode = generateUniqueBarcode(gemBarcode);
        oldGemstone.setUserStatus(GemStatus.FALSE);
        String oldBarcode = oldGemstone.getGemBarcode();
        oldGemstone.setGemBarcode(updatedBarcode);
        gemstoneRepository.save(oldGemstone);

        // Tạo một viên đá quý mới với thông tin cập nhật và giữ nguyên barcode của viên đá quý cũ
        Gemstone newGemstone = new Gemstone();
        newGemstone.setDescription(request.getDescription());
        newGemstone.setPrice(request.getPrice());
        newGemstone.setQuantity(1);
        newGemstone.setCertificateCode(request.getCertificateCode());
        newGemstone.setUserStatus(userStatus); // Sử dụng userStatus từ request param
        newGemstone.setCarat(request.getCarat());
        newGemstone.setColor(request.getColor());
        newGemstone.setClarity(request.getClarity());
        newGemstone.setCut(request.getCut());
        newGemstone.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        newGemstone.setBuyRate(request.getBuyRate());
        newGemstone.setUrl(request.getUrl());
        newGemstone.setGemBarcode(oldBarcode);
        newGemstone.setOldGemID(String.valueOf(oldGemstone.getGemId()));

        return gemstoneRepository.save(newGemstone);
    } else {
        throw new BadRequestException("Gem không tồn tại");
    }
}

//    @Transactional
//    public Gemstone updateGemstoneStatus(String gemBarcode) {
//        Optional<Gemstone> optionalGemstone = gemstoneRepository.findByGemBarcode(gemBarcode);
//        if (optionalGemstone.isPresent()) {
//            Gemstone gemstone = optionalGemstone.get();
//
//            // Kiểm tra trạng thái hiện tại của viên đá quý
//            if (gemstone.getUserStatus() == GemStatus.USE) {
//                throw new BadRequestException("Không thể cập nhật trạng thái của viên đá đã được sử dụng.");
//            }
//
//            // Cập nhật trạng thái
//            if (gemstone.getUserStatus() == GemStatus.FALSE) {
//                gemstone.setUserStatus(GemStatus.NOTUSE);
//            } else {
//                gemstone.setUserStatus(GemStatus.FALSE);
//            }
//
//            // Lưu và trả về viên đá quý đã được cập nhật
//            return gemstoneRepository.save(gemstone);
//        } else {
//            throw new BadRequestException("Không tìm thấy viên đá quý với barcode đã nhập.");
//        }
//    }

     public Gemstone getGemstoneByBarcode(String gemBarcode) {
        return gemstoneRepository.findByGemBarcode(gemBarcode)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy viên đá quý với barcode đã nhập."));
    }

    public List<Gemstone> getGemstonesByStatus(GemStatus status) {
        return gemstoneRepository.findByUserStatus(status);
    }

     // Lấy danh sách tất cả Gemstones
    public List<Gemstone> getAllGemstones() {
        return gemstoneRepository.findAll();
    }
    private String generateUniqueBarcode(String existingBarcode) {
        // Example: Add a prefix "UP:" followed by a unique string to mark an update
        String prefix = "UP:";
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniqueString;
    }


}