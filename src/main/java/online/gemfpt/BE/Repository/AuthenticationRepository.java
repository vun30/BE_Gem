package online.gemfpt.BE.Repository;

import online.gemfpt.BE.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationRepository  extends JpaRepository<Account,Long> {
    Account findAccountByEmail (String email);// tim account bang sdt
    Account findAccountByPhone(String phone);


   // List<Account> findAccounts();
            // lấy 1 account thì find account - lấy 1 danh sách thì thêm s

}
