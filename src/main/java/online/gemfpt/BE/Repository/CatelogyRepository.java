package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Catelogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatelogyRepository extends JpaRepository<Catelogy,Long> {
}
