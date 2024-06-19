package online.gemfpt.BE.api;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.Service.AuthenticationService;
import online.gemfpt.BE.Service.EmailService;
import online.gemfpt.BE.enums.RoleEnum;
import online.gemfpt.BE.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


// nhan request tu fontend
@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    EmailService emailService;

    @PostMapping("/login_google")
    public ResponseEntity<AccountResponse> loginGoogle (@RequestBody LoginGoogleRequest loginGoogleRequest){
        return ResponseEntity.ok(authenticationService.loginGoogle(loginGoogleRequest));
    }
    @PostMapping("/forgot_password")
    public void forgotPassword (@RequestBody ForGotPasswordRequest forGotPasswordRequest) {
        authenticationService.ForGotPassword(forGotPasswordRequest);
    }
    @PostMapping("/reset_password")
    public void resetpassword (@RequestBody ResetPasswordRequest resetwordRequest) {
        authenticationService.ResetPassword(resetwordRequest);
    }

      @DeleteMapping("/delete_account/{email}")
    public ResponseEntity<Account> deleteAccountByEmail(@PathVariable String email) {
        // Lấy thông tin người dùng hiện tại từ context
        Account currentAccount = authenticationService.getCurrentAccount();

        // Kiểm tra xem người dùng hiện tại có vai trò là ADMIN hay không
        if (currentAccount.getRole() != RoleEnum.ADMIN) {
            // Nếu không phải ADMIN, trả về lỗi hoặc xử lý phù hợp
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Tiếp tục xử lý chỉ khi người dùng có vai trò ADMIN
        Account account = authenticationService.deleteAccountByEmail(email);
        return ResponseEntity.ok(account);
    }
    @PutMapping("/admin_edit_account/{email}")
    public ResponseEntity<Account> editAccountByEmail(@PathVariable String email, @RequestBody EditAccountRequest editAccountRequest) {
        // Lấy thông tin người dùng hiện tại từ context
        Account currentAccount = authenticationService.getCurrentAccount();

        // Kiểm tra xem người dùng hiện tại có vai trò là ADMIN hay không
        if (currentAccount.getRole() != RoleEnum .ADMIN) {
            // Nếu không phải ADMIN, trả về lỗi hoặc xử lý phù hợp
            return ResponseEntity.status(HttpStatus .FORBIDDEN).build();
        }

        // Tiếp tục xử lý chỉ khi người dùng có vai trò ADMIN
        Account account = authenticationService.editAccountByEmail(email, editAccountRequest);
        return ResponseEntity.ok(account);
    }
     @PutMapping("/staff_edit_account/{email}")
    public ResponseEntity<Account> staffEditAccountByEmail(@PathVariable String email, @RequestBody StaffEditAccountRequest staffEditAccountRequest) {
           if (!authenticationService.isCurrentAccount(email)) {
            // Nếu không phải chính tài khoản đang đăng nhập, trả về lỗi hoặc xử lý phù hợp
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Account account = authenticationService.staffEditAccountByEmail(email, staffEditAccountRequest);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/admin_only")
    public ResponseEntity getAdmin(){return  ResponseEntity.ok("ok");}

    @PostMapping("/register")
    public ResponseEntity Register (@RequestBody RegisterRequest responseRequest){
        Account  account = authenticationService.register(responseRequest);
        return  ResponseEntity.ok(account);
    }
    @GetMapping("/send_mail")
    public void sendMail(){
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient("baoyasuohoang@gmail.com");
        emailDetail.setSubject("test123");
        emailDetail.setMsgBody("aaa");
        emailService.sendMailTemplate(emailDetail);
    }
    @GetMapping("/getAll")
    public ResponseEntity Getallaccount (){
         // Lấy thông tin người dùng hiện tại từ context
        Account currentAccount = authenticationService.getCurrentAccount();

        // Kiểm tra xem người dùng hiện tại có vai trò là ADMIN hay không
        if (currentAccount.getRole() != RoleEnum .ADMIN) {
            // Nếu không phải ADMIN, trả về lỗi hoặc xử lý phù hợp
            return ResponseEntity.status(HttpStatus .FORBIDDEN).build();
        }

        List<Account> account = authenticationService.all();
        return  ResponseEntity.ok(account);
    }


    @PostMapping("/login")
    public ResponseEntity login (@RequestBody LoginRequest loginRequest){

        Account account = authenticationService.login(loginRequest);
        return ResponseEntity.ok(account);
    }
}
