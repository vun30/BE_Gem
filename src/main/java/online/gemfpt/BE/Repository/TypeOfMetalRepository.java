package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.TypeOfMetal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeOfMetalRepository extends JpaRepository<TypeOfMetal, Long> {
    Optional<TypeOfMetal> findByMetalType(String metalType);
}
