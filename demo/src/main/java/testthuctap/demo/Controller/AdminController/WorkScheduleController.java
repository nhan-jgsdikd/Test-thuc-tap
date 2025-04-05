package testthuctap.demo.Controller.AdminController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import testthuctap.demo.Model.User;
import testthuctap.demo.Model.WorkSchedule;
import testthuctap.demo.Service.WorkScheduleService;

@Controller
@RequestMapping("/Admin/WorkSchedule")
public class WorkScheduleController {

    @Autowired
    private WorkScheduleService workScheduleService;

    // Hiển thị danh sách nhân viên với tổng giờ làm
    @GetMapping
    public String adminHome(Model model) {
        List<WorkSchedule> allWorkSchedules = workScheduleService.getAllWorkSchedules();
        if (allWorkSchedules == null) {
            allWorkSchedules = new ArrayList<>(); // Đảm bảo không null
        }

        // Lấy danh sách nhân viên duy nhất từ WorkSchedule
        List<User> users = allWorkSchedules.stream()
                .map(WorkSchedule::getUser)
                .distinct() // Loại bỏ nhân viên trùng lặp
                .collect(Collectors.toList());

        // Tính tổng giờ làm cho từng nhân viên dựa trên totalHours
        Map<Long, Double> userTotalHours = new HashMap<>();
        for (WorkSchedule ws : allWorkSchedules) {
            Long userId = ws.getUser().getId();
            Double totalHours = ws.getTotalHours() != null ? ws.getTotalHours() : 0.0;
            userTotalHours.put(userId, userTotalHours.getOrDefault(userId, 0.0) + totalHours);
        }

        model.addAttribute("users", users);
        model.addAttribute("userTotalHours", userTotalHours);
        return "Admin/WorkSchedule";
    }

    // Hiển thị chi tiết giờ làm việc của một nhân viên
    @GetMapping("/detail")
    public String showUserWorkSchedule(@RequestParam("userId") Long userId, Model model) {
        List<WorkSchedule> workingHours = workScheduleService.getWorkSchedulesByUserId(userId); // Lấy WorkSchedule theo userId
        if (workingHours == null || workingHours.isEmpty()) {
            workingHours = new ArrayList<>();
            model.addAttribute("error", "Không có dữ liệu giờ làm việc cho nhân viên này.");
        } else {
            User user = workingHours.get(0).getUser(); // Lấy thông tin User từ bản ghi đầu tiên
            model.addAttribute("user", user);
        }

        model.addAttribute("workingHours", workingHours);
        return "Admin/WorkScheduleDetail";
    }

    // Form thêm WorkSchedule
    @GetMapping("/add")
    public String addWorkScheduleForm(Model model) {
        model.addAttribute("workSchedule", new WorkSchedule());
        return "Admin/WorkingHoursAdd"; // Trang thêm mới
    }

    // Xử lý thêm WorkSchedule
    @PostMapping("/add")
    public String addWorkSchedule(@ModelAttribute("workSchedule") WorkSchedule workSchedule, Model model) {
        workScheduleService.saveWorkSchedule(workSchedule);
        model.addAttribute("success", "Thêm giờ làm việc thành công!");
        return "redirect:/Admin/WorkSchedule";
    }

    // Form chỉnh sửa WorkSchedule
    @GetMapping("/edit")
    public String editWorkScheduleForm(@RequestParam("id") Long id, Model model) {
        WorkSchedule workSchedule = workScheduleService.getAllWorkSchedules()
                .stream()
                .filter(ws -> ws.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy WorkSchedule với ID: " + id));
        model.addAttribute("workSchedule", workSchedule);
        return "Admin/WorkingHoursEdit"; // Trang chỉnh sửa
    }

    // Xử lý chỉnh sửa WorkSchedule
    @PostMapping("/edit")
    public String editWorkSchedule(@ModelAttribute("workSchedule") WorkSchedule workSchedule, Model model) {
        workScheduleService.saveWorkSchedule(workSchedule);
        model.addAttribute("success", "Cập nhật giờ làm việc thành công!");
        return "redirect:/Admin/WorkSchedule";
    }

    // Xóa WorkSchedule
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteWorkSchedule(@PathVariable("id") Long id) {
        workScheduleService.deleteWorkSchedule(id);
        return "OK"; // Trả về phản hồi cho fetch
    }

    // Endpoint để xử lý check-in
    @PostMapping("/checkin")
    public String checkIn(@RequestParam("userId") Long userId, Model model) {
        try {
            WorkSchedule workSchedule = workScheduleService.checkIn(userId);
            model.addAttribute("success", "Check-in thành công cho nhân viên ID: " + userId);
            return "redirect:/Admin/WorkSchedule/detail?userId=" + userId;
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi khi check-in: " + e.getMessage());
        }
        return "redirect:/Admin/WorkSchedule";
    }

    // Endpoint để xử lý check-out
    @PostMapping("/checkout")
    public String checkOut(@RequestParam("workScheduleId") Long workScheduleId, Model model) {
        try {
            WorkSchedule workSchedule = workScheduleService.checkOut(workScheduleId);
            Long userId = workSchedule.getUser().getId();
            model.addAttribute("success", "Check-out thành công. Tổng giờ làm việc: " + workSchedule.getTotalHours());
            return "redirect:/Admin/WorkSchedule/detail?userId=" + userId;
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi khi check-out: " + e.getMessage());
        }
        return "redirect:/Admin/WorkSchedule";
    }

    // Endpoint để lấy tổng giờ làm việc
    @GetMapping("/totalhours")
    @ResponseBody
    public String getTotalHours(@RequestParam("workScheduleId") Long workScheduleId) {
        try {
            double totalHours = workScheduleService.getTotalHours(workScheduleId);
            return String.format("Tổng giờ làm việc: %.2f", totalHours);
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}