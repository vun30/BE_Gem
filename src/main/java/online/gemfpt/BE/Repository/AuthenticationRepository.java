package online.gemfpt.BE.Repository;

import online.gemfpt.BE.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationRepository  extends JpaRepository<Account,Long> {
    Account findAccountByPhone(String phone);
    Account findByEmail (String email);// tim account bang sdt

   // List<Account> findAccounts();
            // lấy 1 account thì find account - lấy 1 danh sách thì thêm s

}
