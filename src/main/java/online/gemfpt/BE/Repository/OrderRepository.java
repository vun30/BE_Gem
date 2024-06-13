package online.gemfpt.BE.Repository;


import online.gemfpt.BE.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
