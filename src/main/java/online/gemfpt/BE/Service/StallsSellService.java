package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.BillRepository;
import online.gemfpt.BE.Repository.StallsSellRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.StallsSell;
import online.gemfpt.BE.exception.AccountNotFoundException;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import online.gemfpt.BE.model.AccountOnStallsRequest;
import online.gemfpt.BE.model.StallsSellRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StallsSellService {

    @Autowired
    private StallsSellRepository stallsSellRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private BillRepository billRepository;

    public StallsSell createStalls(StallsSellRequest stallsSellRequest) {
        StallsSell stallsSell = new StallsSell();
        stallsSell.setStallsSellName(stallsSellRequest.getStallsSellName());
        stallsSell.setStallsSellCreateTime(LocalDateTime.now());
        stallsSell.setStallsSellStatus(stallsSellRequest.isStallsSellStatus());
        return stallsSellRepository.save(stallsSell);
    }


    @Transactional
    public Account addAccountOnStalls(Long accountId, AccountOnStallsRequest accountOnStallsRequest) {
        try {
            // Tìm account dựa trên accountId
            Account account = authenticationRepository.findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

            // Kiểm tra xem accountOnStallsRequest có chứa stallsWorkingId hay không
            if (accountOnStallsRequest.getStallsWorkingId() != null) {
                // Kiểm tra xem stallsId có tồn tại trong StallsSellRepository không
                Optional<StallsSell> optionalStallsSell = stallsSellRepository.findById(accountOnStallsRequest.getStallsWorkingId());
                if (!optionalStallsSell.isPresent()) {
                    throw new StallsSellNotFoundException("StallsSell not found with ID: " + accountOnStallsRequest.getStallsWorkingId());
                }
                StallsSell stallsSell = optionalStallsSell.get();

                // Cập nhật thông tin của account
                account.setStallsWorkingId(accountOnStallsRequest.getStallsWorkingId());
            }

            account.setStaffWorkingStatus(accountOnStallsRequest.isStaffWorkingStatus());

            if (accountOnStallsRequest.getStartWorkingDateTime() != null) {
                account.setStartWorkingDateTime(accountOnStallsRequest.getStartWorkingDateTime());
            }
            if (accountOnStallsRequest.getEndWorkingDateTime() != null) {
                account.setEndWorkingDateTime(accountOnStallsRequest.getEndWorkingDateTime());
            }

            // Lưu thông tin cập nhật vào cơ sở dữ liệu và trả về account đã được cập nhật
            return authenticationRepository.save(account);
        } catch (AccountNotFoundException ex) {
            throw ex; // Ném lại ngoại lệ AccountNotFoundException để xử lý ở phần gọi hàm
        } catch (StallsSellNotFoundException ex) {
            throw ex; // Ném lại ngoại lệ StallsSellNotFoundException để xử lý ở phần gọi hàm
        } catch (Exception ex) {
            throw new AccountNotFoundException("Failed to update account on stalls", ex);
        }
    }

    public List<Account> getAllAccounts() {
        return authenticationRepository.findAll();
    }

    public List<Account> getAllActiveStaffAccounts() {
        return authenticationRepository.findAll().stream()
                .filter(Account::isStaffWorkingStatus)
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByWorkingDate(LocalDateTime workingDateTime) {
        return authenticationRepository.findAll().stream()
                .filter(account -> account.getStartWorkingDateTime() != null &&
                        account.getEndWorkingDateTime() != null &&
                        !account.getStartWorkingDateTime().isAfter(workingDateTime) &&
                        !account.getEndWorkingDateTime().isBefore(workingDateTime))
                .collect(Collectors.toList());
    }

    public Account getCurrentAccountWorkShift() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Lấy email từ thông tin đăng nhập
        Account account = authenticationRepository.findAccountByEmail(email);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with email: " + email);
        }
        return account;
    }

    public List<Account> getAccountsByWorkingDateAndStallsId(LocalDateTime workingDateTime, Long stallsWorkingId) {
        return authenticationRepository.findAll().stream()
                .filter(account -> account.getStallsWorkingId() != null &&
                        account.getStallsWorkingId().equals(stallsWorkingId) &&
                        account.getStartWorkingDateTime() != null &&
                        account.getEndWorkingDateTime() != null &&
                        !account.getStartWorkingDateTime().isAfter(workingDateTime) &&
                        !account.getEndWorkingDateTime().isBefore(workingDateTime))
                .collect(Collectors.toList());
    }

    public List<LocalDateTime> getWorkingDatesByAccountId(Long accountId) {
        Account account = authenticationRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        List<LocalDateTime> workingDates = new ArrayList<>();
        LocalDateTime currentDateTime = account.getStartWorkingDateTime();

        while (!currentDateTime.isAfter(account.getEndWorkingDateTime())) {
            workingDates.add(currentDateTime);
            currentDateTime = currentDateTime.plusDays(1); // Có thể thay đổi thành plusHours(1) hoặc bất kỳ đơn vị thời gian nào phù hợp
        }

        return workingDates;
    }

    public Map<String, Object> getTotalRevenueStall(Long stallId) {
        List<Bill> bills = billRepository.findByStalls(stallId);
        double totalRevenue = 0;
        Map<String, Integer> staffOrderCount = new HashMap<>();

        for (Bill bill : bills) {
            totalRevenue += bill.getTotalAmount();
            String cashier = bill.getCashier();
            staffOrderCount.put(cashier, staffOrderCount.getOrDefault(cashier, 0) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stallId", stallId);
        result.put("totalRevenue", totalRevenue);
        result.put("staffOrderCount", staffOrderCount);

        return result;
    }


    public Map<String, Object> getTotalByStaff(String cashier) {
        List<Bill> bills = billRepository.findByCashier(cashier);
        double totalRevenue = 0;
        int orderCount = bills.size();

        for (Bill bill : bills) {
            totalRevenue += bill.getTotalAmount();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cashier", cashier);
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);

        return result;
    }


    public List<Map<String, Object>> getAllStallsRevenue() {
        List<StallsSell> stallsList = stallsSellRepository.findAll();
        List<Map<String, Object>> stallsRevenueList = new ArrayList<>();
        for (StallsSell stalls : stallsList) {
            List<Bill> bills = billRepository.findByStalls(stalls.getStallsSellId());
            double totalRevenue = 0;
            Map<String, Integer> staffOrderCount = new HashMap<>();

            for (Bill bill : bills) {
                totalRevenue += bill.getTotalAmount();
                String cashier = bill.getCashier();
                staffOrderCount.put(cashier, staffOrderCount.getOrDefault(cashier, 0) + 1);
            }

            Map<String, Object> stallsRevenue = new HashMap<>();
            stallsRevenue.put("stallsId", stalls.getStallsSellId());
            stallsRevenue.put("stallsName", stalls.getStallsSellName());
            stallsRevenue.put("totalRevenue", totalRevenue);
            stallsRevenue.put("staffOrderCount", staffOrderCount);
            stallsRevenueList.add(stallsRevenue);
        }
        return stallsRevenueList;
    }


    public List<Map<String, Object>> getTotalByStaff() {
        List<Account> accountList = authenticationRepository.findAll();
        List<Map<String, Object>> employeesRevenueList = new ArrayList<>();
        for (Account account : accountList) {
            List<Bill> bills = billRepository.findByCashier(account.getName());
            double totalRevenue = 0;
            int orderCount = bills.size();

            for (Bill bill : bills) {
                totalRevenue += bill.getTotalAmount();
            }

            Map<String, Object> employeeRevenue = new HashMap<>();
            employeeRevenue.put("employeeId", account.getId());
            employeeRevenue.put("employeeName", account.getName());
            employeeRevenue.put("totalRevenue", totalRevenue);
            employeeRevenue.put("orderCount", orderCount);
            employeesRevenueList.add(employeeRevenue);
        }
        return employeesRevenueList;
    }

    public Map<String, Object> getMonthlyRevenueStall(Long stallId, YearMonth yearMonth) {
        List<Bill> bills = billRepository.findByStalls(stallId).stream()
                .filter(bill -> YearMonth.from(bill.getCreateTime()).equals(yearMonth))
                .collect(Collectors.toList());

        double totalRevenue = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        Map<String, Integer> staffOrderCount = new HashMap<>();
        for (Bill bill : bills) {
            String cashier = bill.getCashier();
            staffOrderCount.put(cashier, staffOrderCount.getOrDefault(cashier, 0) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stallId", stallId);
        result.put("yearMonth", yearMonth.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("staffOrderCount", staffOrderCount);

        return result;
    }

    public Map<String, Object> getYearlyRevenueStall(Long stallId, Year year) {
        List<Bill> bills = billRepository.findByStalls(stallId).stream()
                .filter(bill -> Year.from(bill.getCreateTime()).equals(year))
                .collect(Collectors.toList());

        double totalRevenue = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        Map<String, Integer> staffOrderCount = new HashMap<>();
        for (Bill bill : bills) {
            String cashier = bill.getCashier();
            staffOrderCount.put(cashier, staffOrderCount.getOrDefault(cashier, 0) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stallId", stallId);
        result.put("year", year.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("staffOrderCount", staffOrderCount);

        return result;
    }

    public Map<String, Object> getMonthlyRevenueByStaff(String cashier, YearMonth yearMonth) {
        List<Bill> bills = billRepository.findByCashier(cashier).stream()
                .filter(bill -> YearMonth.from(bill.getCreateTime()).equals(yearMonth))
                .collect(Collectors.toList());

        double totalRevenue = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        int orderCount = bills.size();

        Map<String, Object> result = new HashMap<>();
        result.put("cashier", cashier);
        result.put("yearMonth", yearMonth.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);

        return result;
    }

    public Map<String, Object> getYearlyRevenueByStaff(String cashier, Year year) {
        List<Bill> bills = billRepository.findByCashier(cashier).stream()
                .filter(bill -> Year.from(bill.getCreateTime()).equals(year))
                .collect(Collectors.toList());

        double totalRevenue = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        int orderCount = bills.size();

        Map<String, Object> result = new HashMap<>();
        result.put("cashier", cashier);
        result.put("year", year.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);

        return result;
    }

}