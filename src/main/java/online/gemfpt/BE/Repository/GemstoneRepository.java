package online.gemfpt.BE.Repository;

import online.gemfpt.BE.Entity.Gemstone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GemstoneRepository extends JpaRepository<Gemstone, Long> {
}

