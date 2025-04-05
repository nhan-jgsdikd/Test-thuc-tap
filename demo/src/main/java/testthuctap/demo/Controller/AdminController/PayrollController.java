package testthuctap.demo.Controller.AdminController;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testthuctap.demo.Model.Payroll;
import testthuctap.demo.Model.User;
import testthuctap.demo.Service.PayrollService;
import testthuctap.demo.Service.UserService;
import testthuctap.demo.Service.WorkScheduleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/Admin/Payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final UserService userService;
    private final WorkScheduleService workScheduleService;

    // Hiển thị danh sách tất cả người dùng để quản lý lương
    @GetMapping
    public String listUsers(Model model) {
        List<User> allUsers = userService.getAllUsers();
        List<User> employeeUsers = allUsers.stream()
                .filter(user -> user.getRole().equals("EMPLOYEE"))
                .collect(Collectors.toList());
        model.addAttribute("users", employeeUsers);
        return "Admin/Payroll";
    }

    // Xem chi tiết bảng lương của một người dùng
    @GetMapping("/detail/{userId}")
    public String viewUserPayroll(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Payroll";
        }

        List<Payroll> payrollList = payrollService.getPayrollByUserId(userId);
        model.addAttribute("user", userOpt.get());
        model.addAttribute("payrollList", payrollList);
        return "Admin/PayrollDetail";
    }

    // Hiển thị form thêm bảng lương mới
    @GetMapping("/add/{userId}")
    public String showAddPayrollForm(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Payroll";
        }
        model.addAttribute("user", userOpt.get());
        model.addAttribute("payroll", new Payroll());
        // Truyền totalHours để hiển thị trên form nếu cần
        String currentMonth = LocalDateTime.now().getYear() + "-" + String.format("%02d", LocalDateTime.now().getMonthValue());
        model.addAttribute("totalHours", workScheduleService.getTotalHoursByUserAndMonth(userId, currentMonth));
        return "Admin/PayrollAdd";
    }

    // Xử lý thêm bảng lương mới
    @PostMapping("/add/{userId}")
    public String addPayroll(@PathVariable Long userId,
                             @RequestParam String payrollId,
                             @RequestParam String month,
                             @RequestParam(required = false, defaultValue = "0.0") double bonus,
                             @RequestParam(required = false, defaultValue = "0.0") double deduction,
                             Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Payroll";
        }

        try {
            // Tính tổng totalHours từ WorkSchedule và nhân với 30 để ra baseSalary
            double totalHours = workScheduleService.getTotalHoursByUserAndMonth(userId, month);
            if (totalHours == 0) {
                model.addAttribute("error", "Không có dữ liệu giờ làm việc cho tháng " + month);
                return "redirect:/Admin/Payroll/add/" + userId;
            }
            double baseSalary = totalHours * 30;
            double totalSalary = baseSalary + bonus - deduction;

            Payroll payroll = Payroll.builder()
                    .payrollId(payrollId)
                    .user(userOpt.get())
                    .month(month)
                    .baseSalary(baseSalary)
                    .bonus(bonus)
                    .deduction(deduction)
                    .totalSalary(totalSalary) // Tổng lương được tính và lưu vào database
                    .status("Chưa thanh toán")
                    .createdAt(LocalDateTime.now())
                    .build();

            payrollService.createPayroll(payroll); // Lưu vào database
            model.addAttribute("success", "Thêm bảng lương thành công cho " + userOpt.get().getUsername());
            return "redirect:/Admin/Payroll/detail/" + userId;
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/Admin/Payroll/add/" + userId;
        }
    }

    // Hiển thị form chỉnh sửa bảng lương
    @GetMapping("/edit/{payrollId}")
    public String showEditPayrollForm(@PathVariable Long payrollId, Model model) {
        Optional<Payroll> payrollOpt = payrollService.getPayrollById(payrollId);
        if (payrollOpt.isEmpty()) {
            model.addAttribute("error", "Bảng lương không tồn tại");
            return "redirect:/Admin/Payroll";
        }
        Payroll payroll = payrollOpt.get();
        model.addAttribute("payroll", payroll);
        // Truyền totalHours để hiển thị trên form
        model.addAttribute("totalHours", workScheduleService.getTotalHoursByUserAndMonth(payroll.getUser().getId(), payroll.getMonth()));
        return "Admin/PayrollEdit";
    }

    // Xử lý cập nhật bảng lương
    @PostMapping("/edit/{payrollId}")
    public String updatePayroll(@PathVariable Long payrollId,
                                @RequestParam String payrollIdStr,
                                @RequestParam String month,
                                @RequestParam(required = false, defaultValue = "0.0") double bonus,
                                @RequestParam(required = false, defaultValue = "0.0") double deduction,
                                @RequestParam String status,
                                Model model) {
        Optional<Payroll> payrollOpt = payrollService.getPayrollById(payrollId);
        if (payrollOpt.isEmpty()) {
            model.addAttribute("error", "Bảng lương không tồn tại");
            return "redirect:/Admin/Payroll";
        }

        Payroll payroll = payrollOpt.get();
        try {
            // Tính lại baseSalary dựa trên totalHours
            double totalHours = workScheduleService.getTotalHoursByUserAndMonth(payroll.getUser().getId(), month);
            if (totalHours == 0) {
                model.addAttribute("error", "Không có dữ liệu giờ làm việc cho tháng " + month);
                return "redirect:/Admin/Payroll/edit/" + payrollId;
            }
            double baseSalary = totalHours * 30;
            double totalSalary = baseSalary + bonus - deduction;

            payroll.setPayrollId(payrollIdStr);
            payroll.setMonth(month);
            payroll.setBaseSalary(baseSalary);
            payroll.setBonus(bonus);
            payroll.setDeduction(deduction);
            payroll.setTotalSalary(totalSalary); // Tổng lương được cập nhật và lưu vào database
            payroll.setStatus(status);

            payrollService.updatePayroll(payroll); // Lưu vào database
            model.addAttribute("success", "Cập nhật bảng lương thành công!");
            return "redirect:/Admin/Payroll/detail/" + payroll.getUser().getId();
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/Admin/Payroll/edit/" + payrollId;
        }
    }
}