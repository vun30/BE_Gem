package online.gemfpt.BE.Repository;


import online.gemfpt.BE.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
}
