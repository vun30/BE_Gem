package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.StallsSell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StallsSellRepository extends JpaRepository<StallsSell, Long> {
}
