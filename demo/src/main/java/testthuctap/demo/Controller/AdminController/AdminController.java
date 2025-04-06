package testthuctap.demo.Controller.AdminController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import testthuctap.demo.Model.User;
import testthuctap.demo.Model.WorkSchedule;
import testthuctap.demo.Service.UserService;
import testthuctap.demo.Service.WorkScheduleService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/Admin")
public class AdminController {

    private final UserService userService;
    private final WorkScheduleService workScheduleService;
    private static final String UPLOAD_DIR = "D:\\AZMedia\\Test-thuc-tap\\demo\\src\\main\\resources\\static\\img";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    public AdminController(UserService userService, WorkScheduleService workScheduleService) {
        this.userService = userService;
        this.workScheduleService = workScheduleService;
    }

    @GetMapping
    public String adminHome() {
        return "Admin/Adminhome";
    }

    @GetMapping("/employees")
    public String manageEmployees(Model model) {
        List<User> employees = userService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("dateFormatter", DATE_TIME_FORMATTER);
        return "Admin/companystaff";
    }

    // Hiển thị danh sách giờ làm việc
    @GetMapping("/working-hours")
    public String manageWorkingHours(Model model) {
        List<WorkSchedule> workingHours = workScheduleService.getAllWorkSchedules();
        model.addAttribute("workingHours", workingHours);
        model.addAttribute("dateFormatter", DATE_TIME_FORMATTER);
        return "Admin/WorkingHours";
    }

    // Check-in nhân viên
    @PostMapping("/working-hours/check-in/{userId}")
    public String checkIn(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            WorkSchedule workSchedule = workScheduleService.checkIn(userId);
            redirectAttributes.addFlashAttribute("success", 
                "Check-in thành công lúc: " + workSchedule.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi check-in: " + e.getMessage());
        }
        return "redirect:/Admin/working-hours";
    }

    // Check-out nhân viên
    @PostMapping("/working-hours/check-out/{workScheduleId}")
    public String checkOut(@PathVariable Long workScheduleId, RedirectAttributes redirectAttributes) {
        try {
            WorkSchedule workSchedule = workScheduleService.checkOut(workScheduleId);
            redirectAttributes.addFlashAttribute("success", 
                "Check-out thành công. Tổng giờ làm: " + workSchedule.getTotalHours() + " giờ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi check-out: " + e.getMessage());
        }
        return "redirect:/Admin/working-hours";
    }

    // Các phương thức khác giữ nguyên
    @GetMapping("/employees/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        return "Admin/add-employee";
    }

    @PostMapping("/employees/add")
    public String addEmployee(
            @ModelAttribute("user") User user,
            @RequestParam("avatarFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        // Giữ nguyên logic cũ
        try {
            if (!file.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                file.transferTo(new File(uploadDir, fileName));
                user.setAvatar("/img/" + fileName);
            }
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setStatus("ACTIVE");
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", 
                "Thêm nhân viên thành công lúc: " + now.format(DATE_TIME_FORMATTER));
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/Admin/employees";
    }

    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("dateFormatter", DATE_TIME_FORMATTER);
        return "Admin/edit-employee";
    }

    @PostMapping("/employees/edit/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @ModelAttribute("user") User updatedUser,
            @RequestParam(value = "avatarFile", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {
        // Giữ nguyên logic cũ
        try {
            User existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            existingUser.setUserId(updatedUser.getUserId());
            existingUser.setUsername(updatedUser.getUsername());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(updatedUser.getPassword());
            }
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
            existingUser.setPosition(updatedUser.getPosition());
            existingUser.setHireDate(updatedUser.getHireDate());
            existingUser.setStatus(updatedUser.getStatus());
            existingUser.setUpdatedAt(LocalDateTime.now());
            if (file != null && !file.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                file.transferTo(new File(uploadDir, fileName));
                existingUser.setAvatar("/img/" + fileName);
            }
            userService.saveUser(existingUser);
            redirectAttributes.addFlashAttribute("success", 
                "Cập nhật thành công lúc: " + existingUser.getUpdatedAt().format(DATE_TIME_FORMATTER));
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/Admin/employees";
    }

    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", 
                "Đã xóa nhân viên lúc: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/Admin/employees";
    }
}