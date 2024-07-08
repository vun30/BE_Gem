package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.TypeOfMetal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeOfMetalRepository extends JpaRepository<TypeOfMetal, Long> {
   // Optional<TypeOfMetal> findByMetalType(String metalType);
     @Query("SELECT t FROM TypeOfMetal t WHERE t.metalType = :metalType AND t.status = true")
    Optional<TypeOfMetal> findByMetalType(@Param("metalType") String metalType);

     List <TypeOfMetal> findByStatus(boolean status);


}
