package online.gemfpt.BE.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import online.gemfpt.BE.Service.StallsSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name="api")
@CrossOrigin("*")
public class RevenueAPI {
    @Autowired
    private StallsSellService stallsSellService;

    @GetMapping("/stall/{stallId}")
    public Map<String, Object> getTotalRevenueStall(@PathVariable Long stallId) {
        return stallsSellService.getTotalRevenueStall(stallId);
    }

    @GetMapping("/staff")
    public Map<String, Object> getTotalByStaff(@RequestParam String cashier) {
        return stallsSellService.getTotalByStaff(cashier);
    }

    @GetMapping("/revenue/stalls")
    public List<Map<String, Object>> getAllStallsRevenue() {
        return stallsSellService.getAllStallsRevenue();
    }

    @GetMapping("/revenue/cashiers")
    public List<Map<String, Object>> getAllEmployeesRevenue() {
        return stallsSellService.getTotalByStaff();
    }

    @GetMapping("/monthly-revenue/{stallId}")
    public Map<String, Object> getMonthlyRevenueStall(@PathVariable Long stallId, @RequestParam String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        return stallsSellService.getMonthlyRevenueStall(stallId, ym);
    }

    @GetMapping("/yearly-revenue/{stallId}")
    public Map<String, Object> getYearlyRevenueStall(@PathVariable Long stallId, @RequestParam String year) {
        Year y = Year.parse(year);
        return stallsSellService.getYearlyRevenueStall(stallId, y);
    }

    @GetMapping("/monthly-revenue/staff/{cashier}")
    public Map<String, Object> getMonthlyRevenueByStaff(@PathVariable String cashier, @RequestParam String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        return stallsSellService.getMonthlyRevenueByStaff(cashier, ym);
    }

    @GetMapping("/yearly-revenue/staff/{cashier}")
    public Map<String, Object> getYearlyRevenueByStaff(@PathVariable String cashier, @RequestParam String year) {
        Year y = Year.parse(year);
        return stallsSellService.getYearlyRevenueByStaff(cashier, y);
    }
}
