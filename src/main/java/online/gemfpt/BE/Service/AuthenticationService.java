package online.gemfpt.BE.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import online.gemfpt.BE.Entity.Account;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.enums.RoleEnum;
import online.gemfpt.BE.exception.AccountNotFoundException;
import online.gemfpt.BE.exception.BadRequestException;
import online.gemfpt.BE.exception.handler.GlobalExceptionHandler;
import online.gemfpt.BE.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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
        account.setCreateDateNow(account.getCreateDate());
        account.setRole(RoleEnum.STAFF);
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        //xử lý logic register

        // Lưu dữ liệu vào cơ sở dữ liệu
        return authenticationRepository.save(account);
    }

    public Account editAccount(EditAccountRequest editAccountRequest) {
        Account account = authenticationRepository.findAccountByEmail(editAccountRequest.getEmail());
        if (account == null) {
            throw new AccountNotFoundException("Account not found");
        }

        // Kiểm tra và chỉ cập nhật các trường không rỗng
        if (editAccountRequest.getName() != null && !editAccountRequest.getName().isEmpty()) {
            account.setName(editAccountRequest.getName());
        }
        if (editAccountRequest.getDescription() != null && !editAccountRequest.getDescription().isEmpty()) {
            account.setDescription(editAccountRequest.getDescription());
        }

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
    public List<Account> all() {
        return authenticationRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return authenticationRepository.findAccountByPhone(phone) ;
    }

    public AccountResponse loginGoogle (LoginGoogleRequest loginGoogleRequest){
        AccountResponse accountResponse= new AccountResponse();
        try{
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(loginGoogleRequest.getToken());
            String email  = firebaseToken.getEmail();
            Account account = authenticationRepository.findAccountByEmail(email);
            if (account == null ){
                account = new Account();
                account.setName(firebaseToken.getName());
                account.setEmail(email);
                account.setRole(RoleEnum.STAFF);
                account.setCreateDate(LocalDateTime.now());
                account = authenticationRepository.save(account);
            }
            accountResponse.setEmail(account.getEmail());
            accountResponse.setId(account.getId());
            accountResponse.setRole(RoleEnum.STAFF);
            accountResponse.setName(account.getName());
            String token = tokenService.generateToken(account);
            accountResponse.setToken(token);
        } catch (Exception e){
            System.out.println(e);
        }
        return accountResponse;
    }

    public void ForGotPassword(ForGotPasswordRequest forGotPasswordRequest) {
        Account account = authenticationRepository.findAccountByEmail(forGotPasswordRequest.getEmail());
        if (account == null){
            try {
             throw new BadRequestException("Account not found !!");
            }catch(RuntimeException e ){
                throw new RuntimeException(e);
            }
        }
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient(account.getEmail());
        emailDetail.setSubject("Reset password for account " + forGotPasswordRequest.getEmail() + "|");
        emailDetail.setMsgBody("");
        emailDetail.setButtonValue("Reset passwprd");
        emailDetail.setLink("" + tokenService.generateToken(account));// thieu link resset password
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
}
