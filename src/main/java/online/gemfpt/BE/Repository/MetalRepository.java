package online.gemfpt.BE.Repository;

import online.gemfpt.BE.Entity.Metal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetalRepository extends JpaRepository<Metal, Long> {
}
