package online.gemfpt.BE.Repository;

import online.gemfpt.BE.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);


    @Query ("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Customer c JOIN c.discountRequests d " +
           "WHERE c.phone = :phone AND d.id = :discountId")
    boolean existsByPhoneAndDiscounts_Id(String phone, Long discountId);
}


