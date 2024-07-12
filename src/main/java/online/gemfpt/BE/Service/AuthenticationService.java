package online.gemfpt.BE.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.enums.RoleEnum;
import online.gemfpt.BE.exception.AccountNotFoundException;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AuthenticationService implements UserDetailsService {
// xu ly logic

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AuthenticationRepository authenticationRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;


    public Account register(RegisterRequest registerRequest) {
        // Kiểm tra các trường bắt buộc
        if (registerRequest.getName() == null || registerRequest.getName().isEmpty() ||
                registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty() ||
                registerRequest.getPhone() == null || registerRequest.getPhone().isEmpty() ||
                registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            throw new BadRequestException("Missing required fields");
        }

        Account account = new Account();
        account.setName(registerRequest.getName());
        account.setEmail(registerRequest.getEmail());
        account.setPhone(registerRequest.getPhone());
        account.setCreateDate(LocalDateTime.now());
        account.setRole(RoleEnum.STAFF);
        account.setStatus(false);
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        //xử lý logic register

        // Lưu dữ liệu vào cơ sở dữ liệu
        return authenticationRepository.save(account);
    }

   public Account editAccountByEmail(String email, EditAccountRequest editAccountRequest) {
    Account account = authenticationRepository.findAccountByEmail(email);
    if (account == null) {
        throw new AccountNotFoundException("Account not found with email: " + email);
    }

    // Update only non-null fields from editAccountRequest
    if (editAccountRequest.getPhone() != null) {
        account.setPhone(editAccountRequest.getPhone());
    }
    if (editAccountRequest.getDescription() != null) {
        account.setDescription("Name: " + editAccountRequest.getName() + " " + "Details:  " + editAccountRequest.getDescription() + " " + "Phone: " + editAccountRequest.getPhone() + " " + "Role"  + " " + editAccountRequest.getRole());
    }
    if (editAccountRequest.isStatus() != account.isStatus()) {
        account.setStatus(editAccountRequest.isStatus());
    }
    if (editAccountRequest.getRole() != null) {
        account.setRole(editAccountRequest.getRole());
    }
    if (editAccountRequest.getName() != null) {
        account.setName(editAccountRequest.getName());
    }

    // Save updated account
    return authenticationRepository.save(account);
}

public Account staffEditAccountByEmail(String email, StaffEditAccountRequest staffEditAccountRequest) {
    Account account = authenticationRepository.findAccountByEmail(email);
    if (account == null) {
        throw new AccountNotFoundException("Account not found with email: " + email);
    }

    // Update only non-null fields from editAccountRequest
    if (staffEditAccountRequest.getPhone() != null) {
        account.setPhone(staffEditAccountRequest.getPhone());
    }
    if (staffEditAccountRequest.getDescription() != null) {
        account.setDescription("Name: " + staffEditAccountRequest.getName() + " " + "Details:  " + staffEditAccountRequest.getDescription() + " " + "Phone: " + staffEditAccountRequest.getPhone() + " " + "Role: "  + " " + account.getRole());
    }
    if (staffEditAccountRequest.getName() != null) {
        account.setName(staffEditAccountRequest.getName());
    }

    // Save updated account
    return authenticationRepository.save(account);
}


     public Account login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Lấy thông tin tài khoản từ cơ sở dữ liệu
        Account account = authenticationRepository.findAccountByEmail(loginRequest.getEmail());
        if (account == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Kiểm tra trạng thái của tài khoản
         if (!account.isStatus()) {
            throw new BadCredentialsException("Account is inactive");
        }

        // Tạo token và tạo đối tượng AccountResponse
        String token = tokenService.generateToken(account);
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setEmail(account.getEmail());
        accountResponse.setToken(token);
        accountResponse.setId(account.getId());
        accountResponse.setPhone(account.getPhone());
        accountResponse.setName(account.getName());
        accountResponse.setRole(account.getRole());
        accountResponse.setCreateDateNow(account.getCreateDate());

        return accountResponse;
    }

        public AccountResponse loginGoogle(LoginGoogleRequest loginGoogleRequest) {
    AccountResponse accountResponse = new AccountResponse();
    try {
        FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(loginGoogleRequest.getToken());
        String email = firebaseToken.getEmail();
        Account account = authenticationRepository.findAccountByEmail(email);
        if (account == null) {
            account = new Account();
            account.setName(firebaseToken.getName());
            account.setUrl(firebaseToken.getPicture());
            account.setEmail(email);
            account.setRole(RoleEnum.STAFF);
            account.setStatus(false);
            account.setCreateDate(LocalDateTime.now());
            account = authenticationRepository.save(account);
        }
        accountResponse.setEmail(account.getEmail());
        accountResponse.setId(account.getId());
        account.setUrl(firebaseToken.getPicture());
        accountResponse.setRole(RoleEnum.STAFF);
        accountResponse.setPhone(account.getPhone());
        accountResponse.setName(account.getName());
        accountResponse.setCreateDateNow(account.getCreateDate());

        String token = tokenService.generateToken(account);
        accountResponse.setToken(token);
    } catch (Exception e) {
        // Handle specific exceptions and log or throw appropriate errors
        System.out.println("Exception occurred during Google login: " + e.getMessage());
        throw new RuntimeException("Error during Google login", e);
    }
    return accountResponse;
}

    public List<Account> all() {
        return authenticationRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authenticationRepository.findAccountByEmail(email) ;
    }

    public Account deleteAccountByEmail(String email) {
        Account account = authenticationRepository.findAccountByEmail(email);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with email: " + email);
        }
        account.setStatus(false);
        return authenticationRepository.save(account);
    }

//    public AccountResponse loginGoogle (LoginGoogleRequest loginGoogleRequest){
//        AccountResponse accountResponse= new AccountResponse();
//        try{
//            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(loginGoogleRequest.getToken());
//            String email  = firebaseToken.getEmail();
//            Account account = authenticationRepository.findAccountByEmail(email);
//            if (account == null ){
//                account = new Account();
//                account.setName(firebaseToken.getName());
//                account.setEmail(email);
//                account.setRole(RoleEnum.STAFF);
//                account.setCreateDate(LocalDateTime.now());
//                account = authenticationRepository.save(account);
//            }
//            accountResponse.setEmail(account.getEmail());
//            accountResponse.setId(account.getId());
//            accountResponse.setRole(RoleEnum.STAFF);
//            accountResponse.setName(account.getName());
//            String token = tokenService.generateToken(account);
//            accountResponse.setToken(token);
//        } catch (Exception e){
//            System.out.println(e);
//        }
//        return accountResponse;
//    }


    public void forGotPassword(ForGotPasswordRequest forGotPasswordRequest) {
    Account account = authenticationRepository.findAccountByEmail(forGotPasswordRequest.getEmail());
    if (account == null) {
        throw new BadRequestException("Account not found !!");
    }

    String token = tokenService.generateToken(account);
    String resetLink = "http://152.42.182.49/reset-password?token="+ token;

    // Prepare email details
    EmailDetail emailDetail = new EmailDetail();
    emailDetail.setRecipient(account.getEmail());
    emailDetail.setSubject("Đặt lại mật khẩu cho tài khoản " + forGotPasswordRequest.getEmail());
    emailDetail.setLink(resetLink);
    emailDetail.setFullname(account.getName());
    emailDetail.setButtonValue("Reset password");

    Runnable r = new Runnable() {
        @Override
        public void run() {
            emailService.sendMailTemplate(emailDetail);
        }
    };
    new Thread(r).start();
}

    public Account getCurrentAccount(){
        return(Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void ResetPassword(ResetPasswordRequest resetPasswordRequest) {
        Account account = getCurrentAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        authenticationRepository.save(account);
    }


     // Phương thức để lấy thông tin người dùng hiện tại
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("ROLE_NOT_FOUND");
        }
        return "ROLE_NOT_FOUND";
    }

    // Phương thức chung để kiểm tra vai trò của người dùng
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(role));
        }
        return false;
    }

    // Kiểm tra xem người dùng có quyền admin hay không
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    // Kiểm tra xem người dùng có quyền manager hay không
    public boolean isManager() {
        return hasRole("MANAGER");
    }

    // Kiểm tra xem người dùng có phải là chính account đang đăng nhập hay không
    public boolean isCurrentAccount(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername().equals(email);
        }
        return false;
    }

      public Account getAccountById(Long id) {
        return authenticationRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + id));
    }

     public Account getAccountByEmail(String email) {
        Account account = authenticationRepository.findAccountByEmail(email);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with email: " + email);
        }
        return account;
    }
}
