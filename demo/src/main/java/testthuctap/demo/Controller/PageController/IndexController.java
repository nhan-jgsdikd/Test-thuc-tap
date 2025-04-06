package testthuctap.demo.Controller.PageController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import testthuctap.demo.Model.Attendance;
import testthuctap.demo.Model.User;
import testthuctap.demo.Service.AttendanceService;
import testthuctap.demo.Service.UserService;
import testthuctap.demo.Service.WorkScheduleService;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class IndexController {

    private final AttendanceService attendanceService;
    private final UserService userService;
    private final WorkScheduleService workScheduleService;

    public IndexController(AttendanceService attendanceService, UserService userService, WorkScheduleService workScheduleService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.workScheduleService = workScheduleService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "Welcome to our website!");
        return "Employee/index";
    }

@GetMapping("/home")
public String home(Model model, HttpSession session) {
    User loggedInUser = getLoggedInUser(session);
    if (loggedInUser == null) return "redirect:/login";

    model.addAttribute("user", loggedInUser);
    List<Attendance> attendanceHistory = attendanceService.getAttendanceByUserId(loggedInUser.getId());
    model.addAttribute("attendanceHistory", attendanceHistory);

    // Tính tổng giờ làm (giả định từ attendanceHistory)
    double totalHours = attendanceHistory.stream()
        .filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
        .mapToDouble(att -> {
            long minutes = java.time.Duration.between(att.getCheckInTime(), att.getCheckOutTime()).toMinutes();
            return minutes / 60.0; // Chuyển sang giờ (số thập phân)
        })
        .sum();
    model.addAttribute("totalHours", totalHours);

    return "Employee/home";
}

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
}
