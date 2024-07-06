package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.AuthenticationRepository;
import online.gemfpt.BE.Repository.BillRepository;
import online.gemfpt.BE.Repository.MoneyChangeHistoryRepository;
import online.gemfpt.BE.Repository.StallsSellRepository;
import online.gemfpt.BE.entity.Account;
import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.MoneyChangeHistory;
import online.gemfpt.BE.entity.StallsSell;
import online.gemfpt.BE.enums.TypeMoneyChange;
import online.gemfpt.BE.exception.AccountNotFoundException;
import online.gemfpt.BE.exception.ProductNotFoundException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import online.gemfpt.BE.model.AccountOnStallsRequest;
import online.gemfpt.BE.model.MoneyChangeRequest;
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

    @Autowired
    private MoneyChangeHistoryRepository moneyChangeHistoryRepository;

     public List<StallsSell> getAllStalls() {
        return stallsSellRepository.findAll();
    }

    public StallsSell createStalls(StallsSellRequest stallsSellRequest) {
        StallsSell stallsSell = new StallsSell();
        stallsSell.setStallsSellName(stallsSellRequest.getStallsSellName());
        stallsSell.setStallsSellCreateTime(LocalDateTime.now());
        stallsSell.setStallsSellStatus(stallsSellRequest.isStallsSellStatus());
        return stallsSellRepository.save(stallsSell);
    }


    @Transactional
public List<Account> addAccountsOnStalls(List<Long> accountIds, AccountOnStallsRequest accountOnStallsRequest) {
    List<Account> updatedAccounts = new ArrayList<>();

    for (Long accountId : accountIds) {
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

            // Lưu thông tin cập nhật vào cơ sở dữ liệu
            updatedAccounts.add(authenticationRepository.save(account));
        } catch (AccountNotFoundException | StallsSellNotFoundException ex) {
            // Xử lý ngoại lệ cụ thể
            throw ex;
        } catch (Exception ex) {
            throw new AccountNotFoundException("Failed to update account on stalls", ex);
        }
    }
    return updatedAccounts;
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

     @Transactional
    public MoneyChangeHistory  changeMoneyInStalls(MoneyChangeRequest  moneyChangeRequest, TypeMoneyChange typeChange) {
        // Lấy thông tin quầy bán theo ID
        StallsSell stallsSell = stallsSellRepository.findById(moneyChangeRequest.getStallsSellId())
                .orElseThrow(() -> new StallsSellNotFoundException("Không tìm thấy quầy bán với ID: " + moneyChangeRequest.getStallsSellId()));

        double amount = moneyChangeRequest.getAmount();

        // Kiểm tra loại giao dịch và điều chỉnh số tiền tương ứng
        if (typeChange == TypeMoneyChange .WITHDRAW) {
            amount = -amount;
        }

        // Cập nhật tổng số tiền trong quầy
        stallsSell.setMoney(stallsSell.getMoney() + amount);
        stallsSellRepository.save(stallsSell);

        // Ghi lại lịch sử thay đổi tiền
        MoneyChangeHistory moneyChangeHistory = new MoneyChangeHistory();
        moneyChangeHistory.setStallsSell(stallsSell);
        moneyChangeHistory.setAmount(amount);
        moneyChangeHistory.setChangeDateTime(LocalDateTime.now());
        moneyChangeHistory.setBillId(moneyChangeRequest.getBillId());
        moneyChangeHistory.setStatus("Hoàn thành");
        moneyChangeHistory.setTypeChange(typeChange);

        // Lưu lại lịch sử thay đổi tiền và trả về bản ghi
        return moneyChangeHistoryRepository.save(moneyChangeHistory);
    }

     public void updateStallsStatus(Long stallsSellId, boolean status) {
        StallsSell stallsSell = stallsSellRepository.findById(stallsSellId)
                .orElseThrow(() -> new StallsSellNotFoundException("Không tìm thấy quầy bán với ID: " + stallsSellId));

        stallsSell.setStallsSellStatus(status);
        stallsSellRepository.save(stallsSell);
    }

    public List<MoneyChangeHistory> getMoneyChangeHistory(Long stallsSellId) {
        return moneyChangeHistoryRepository.findByStallsSell_StallsSellIdOrderByChangeDateTimeDesc(stallsSellId);
    }

    public Map<Long, List<Double>> getMonthlyRevenueForEachStall() {
        List<StallsSell> stallsList = stallsSellRepository.findAll();
        Map<Long, List<Double>> result = new HashMap<>();

        for (StallsSell stall : stallsList) {
            double[] monthlyRevenue = new double[12];
            List<Bill> bills = billRepository.findByStalls(stall.getStallsSellId());
            for (Bill bill : bills) {
                int month = bill.getCreateTime().getMonthValue() - 1;
                monthlyRevenue[month] += bill.getTotalAmount();
            }

            List<Double> revenueList = Arrays.stream(monthlyRevenue).boxed().collect(Collectors.toList());
            result.put(stall.getStallsSellId(), revenueList);
        }

        return result;
    }

    public Map<Long, List<Double>> getYearlyRevenueForEachStall() {
        List<StallsSell> stallsList = stallsSellRepository.findAll();
        Map<Long, List<Double>> result = new HashMap<>();

        for (StallsSell stall : stallsList) {
            double[] yearRevenue = new double[6];
            List<Bill> bills = billRepository.findByStalls(stall.getStallsSellId());
            for (Bill bill : bills) {
                int year = bill.getCreateTime().getYear() - 2019; // Adjust the base year as needed
                if (year >= 0 && year < 6) {
                    yearRevenue[year] += bill.getTotalAmount();
                }
            }

            List<Double> revenueList = Arrays.stream(yearRevenue).boxed().collect(Collectors.toList());
            result.put(stall.getStallsSellId(), revenueList);
        }

        return result;
    }


}