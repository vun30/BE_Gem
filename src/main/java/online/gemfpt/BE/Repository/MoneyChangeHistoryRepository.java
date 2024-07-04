package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.MoneyChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoneyChangeHistoryRepository extends JpaRepository<MoneyChangeHistory, Long> {
    List <MoneyChangeHistory> findByStallsSell_StallsSellIdOrderByChangeDateTimeDesc(Long stallsSellId);
}
