package testthuctap.demo.Controller.AdminController;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import testthuctap.demo.Model.Attendance;
import testthuctap.demo.Model.User;
import testthuctap.demo.Service.AttendanceService;
import testthuctap.demo.Service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/Admin/Attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "Admin/Attendance";
    }

    @GetMapping("/detail/{userId}")
    public String viewUserAttendance(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Attendance";
        }

        List<Attendance> attendanceList = attendanceService.getAttendanceByUserId(userId);
        model.addAttribute("user", userOpt.get());
        model.addAttribute("attendanceList", attendanceList);
        return "Admin/AttendanceDetail";
    }

    @GetMapping("/add/{userId}")
    public String showAddAttendanceForm(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Attendance";
        }
        model.addAttribute("user", userOpt.get());
        return "Admin/AttendanceAdd";
    }

    @PostMapping("/add/{userId}")
    public String addAttendance(@PathVariable Long userId, @RequestParam LocalDate date,
                                @RequestParam LocalTime checkInTime, @RequestParam(required = false) LocalTime checkOutTime,
                                Model model) {
        try {
            Attendance attendance = attendanceService.createAttendance(userId, date, checkInTime, checkOutTime);
            model.addAttribute("success", "Chấm công thành công cho người dùng: " + attendance.getUser().getUsername());
            return "redirect:/Admin/Attendance/detail/" + userId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/Admin/Attendance/add/" + userId;
        }
    }

    @GetMapping("/edit/{attendanceId}")
    public String showEditAttendanceForm(@PathVariable Long attendanceId, Model model) {
        Optional<Attendance> attendanceOpt = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOpt.isEmpty()) {
            model.addAttribute("error", "Chấm công không tồn tại");
            return "redirect:/Admin/Attendance";
        }
        model.addAttribute("attendance", attendanceOpt.get());
        return "Admin/AttendanceEdit";
    }

    @PostMapping("/edit/{attendanceId}")
    public String updateAttendance(@PathVariable Long attendanceId, @RequestParam LocalTime checkInTime,
                                   @RequestParam(required = false) LocalTime checkOutTime, Model model) {
        Optional<Attendance> attendanceOpt = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOpt.isEmpty()) {
            model.addAttribute("error", "Chấm công không tồn tại");
            return "redirect:/Admin/Attendance";
        }

        Attendance attendance = attendanceOpt.get();
        attendance.setCheckInTime(checkInTime);
        attendance.setCheckOutTime(checkOutTime);

        attendanceService.updateAttendance(attendance);
        model.addAttribute("success", "Cập nhật chấm công thành công!");
        return "redirect:/Admin/Attendance/detail/" + attendance.getUser().getId();
    }

    @PostMapping("/checkin/{userId}")
    public String checkIn(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Attendance";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Attendance> todayAttendance = attendanceService.getAttendanceByUserIdAndDate(userId, today);
        if (!todayAttendance.isEmpty()) {
            model.addAttribute("error", "Đã chấm công giờ vào hôm nay!");
            return "redirect:/Admin/Attendance/detail/" + userId;
        }

        Attendance attendance = attendanceService.createAttendance(userId, today, now, null);
        model.addAttribute("success", "Chấm công giờ vào thành công cho " + attendance.getUser().getUsername());
        return "redirect:/Admin/Attendance/detail/" + userId;
    }

    @PostMapping("/checkout/{userId}")
    public String checkOut(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "redirect:/Admin/Attendance";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Attendance> todayAttendance = attendanceService.getAttendanceByUserIdAndDate(userId, today);
        if (todayAttendance.isEmpty()) {
            model.addAttribute("error", "Chưa chấm công giờ vào hôm nay!");
            return "redirect:/Admin/Attendance/detail/" + userId;
        }

        Attendance attendance = todayAttendance.get(0);
        if (attendance.getCheckOutTime() != null) {
            model.addAttribute("error", "Đã chấm công giờ ra hôm nay!");
            return "redirect:/Admin/Attendance/detail/" + userId;
        }

        attendance.setCheckOutTime(now);
        attendanceService.updateAttendance(attendance);
        model.addAttribute("success", "Chấm công giờ ra thành công cho " + attendance.getUser().getUsername());
        return "redirect:/Admin/Attendance/detail/" + userId;
    }
}