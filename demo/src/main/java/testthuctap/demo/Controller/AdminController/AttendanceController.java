package testthuctap.demo.Controller.AdminController;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import testthuctap.demo.Model.Attendance;
import testthuctap.demo.Model.User;
import testthuctap.demo.Service.AttendanceService;
import testthuctap.demo.Service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/Admin/Attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    // Hiển thị danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "Admin/Attendance";
    }

    // Xem chi tiết chấm công của một người dùng
    @GetMapping("/detail/{userId}")
    public String viewUserAttendance(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
            return "redirect:/Admin/Attendance";
        }

        User user = userOpt.get();
        List<Attendance> attendanceList = attendanceService.getAttendanceByUserId(userId);
        model.addAttribute("user", user);
        model.addAttribute("attendanceList", attendanceList);
        return "Admin/AttendanceDetail";
    }

    // Hiển thị form thêm chấm công
    @GetMapping("/add/{userId}")
    public String showAddAttendanceForm(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
            return "redirect:/Admin/Attendance";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("attendance", new Attendance());
        return "Admin/AttendanceAdd";
    }

    // Thêm chấm công mới
    @PostMapping("/add/{userId}")
    public String addAttendance(
            @PathVariable Long userId,
            @ModelAttribute Attendance attendance,
            @RequestParam LocalDate date,
            @RequestParam LocalTime checkInTime,
            @RequestParam(required = false) LocalTime checkOutTime,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
            }

            User user = userOpt.get();
            attendance.setUser(user);
            attendance.setDate(date);
            attendance.setCheckInTime(checkInTime);
            attendance.setCheckOutTime(checkOutTime);
            attendance.setCreatedAt(LocalDateTime.now());

            attendanceService.createAttendance(attendance);
            redirectAttributes.addFlashAttribute("success", "Đã thêm chấm công thành công cho " + user.getUsername());
            return "redirect:/Admin/Attendance/detail/" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm chấm công: " + e.getMessage());
            return "redirect:/Admin/Attendance/add/" + userId;
        }
    }

    // Hiển thị form chỉnh sửa chấm công
    @GetMapping("/edit/{attendanceId}")
    public String showEditAttendanceForm(@PathVariable Long attendanceId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Attendance> attendanceOpt = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy bản ghi chấm công với ID: " + attendanceId);
            return "redirect:/Admin/Attendance";
        }

        Attendance attendance = attendanceOpt.get();
        model.addAttribute("attendance", attendance);
        model.addAttribute("user", attendance.getUser());
        return "Admin/AttendanceEdit";
    }

    // Cập nhật chấm công
    @PostMapping("/edit/{attendanceId}")
    public String updateAttendance(
            @PathVariable Long attendanceId,
            @RequestParam LocalTime checkInTime,
            @RequestParam(required = false) LocalTime checkOutTime,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Attendance> attendanceOpt = attendanceService.getAttendanceById(attendanceId);
            if (attendanceOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy bản ghi chấm công với ID: " + attendanceId);
            }

            Attendance attendance = attendanceOpt.get();
            attendance.setCheckInTime(checkInTime);
            attendance.setCheckOutTime(checkOutTime);
            attendance.setUpdatedAt(LocalDateTime.now());

            attendanceService.updateAttendance(attendance);
            redirectAttributes.addFlashAttribute("success", "Cập nhật chấm công thành công!");
            return "redirect:/Admin/Attendance/detail/" + attendance.getUser().getUserId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật chấm công: " + e.getMessage());
            return "redirect:/Admin/Attendance/edit/" + attendanceId;
        }
    }

    // Chấm công giờ vào
    @PostMapping("/checkin/{userId}")
    public String checkIn(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
            }

            User user = userOpt.get();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            Optional<Attendance> todayAttendanceOpt = attendanceService.getTodayAttendance(userId);
            if (todayAttendanceOpt.isPresent() && todayAttendanceOpt.get().getCheckInTime() != null) {
                redirectAttributes.addFlashAttribute("error", "Hôm nay đã chấm công giờ vào cho " + user.getUsername());
                return "redirect:/Admin/Attendance/detail/" + userId;
            }

            Attendance attendance = todayAttendanceOpt.orElseGet(() -> Attendance.builder()
                    .user(user)
                    .date(today)
                    .createdAt(LocalDateTime.now())
                    .build());
            attendance.setCheckInTime(now);

            attendanceService.createAttendance(attendance);
            redirectAttributes.addFlashAttribute("success", "Chấm công giờ vào thành công lúc " + now);
            return "redirect:/Admin/Attendance/detail/" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chấm công giờ vào: " + e.getMessage());
            return "redirect:/Admin/Attendance";
        }
    }

    // Chấm công giờ ra
    @PostMapping("/checkout/{userId}")
    public String checkOut(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
            }

            User user = userOpt.get();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            Optional<Attendance> todayAttendanceOpt = attendanceService.getTodayAttendance(userId);
            if (todayAttendanceOpt.isEmpty() || todayAttendanceOpt.get().getCheckInTime() == null) {
                redirectAttributes.addFlashAttribute("error", "Chưa chấm công giờ vào hôm nay cho " + user.getUsername());
                return "redirect:/Admin/Attendance/detail/" + userId;
            }

            Attendance attendance = todayAttendanceOpt.get();
            if (attendance.getCheckOutTime() != null) {
                redirectAttributes.addFlashAttribute("error", "Hôm nay đã chấm công giờ ra cho " + user.getUsername());
                return "redirect:/Admin/Attendance/detail/" + userId;
            }

            attendance.setCheckOutTime(now);
            attendance.setUpdatedAt(LocalDateTime.now());
            attendanceService.updateAttendance(attendance);
            redirectAttributes.addFlashAttribute("success", "Chấm công giờ ra thành công lúc " + now);
            return "redirect:/Admin/Attendance/detail/" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chấm công giờ ra: " + e.getMessage());
            return "redirect:/Admin/Attendance";
        }
    }
}