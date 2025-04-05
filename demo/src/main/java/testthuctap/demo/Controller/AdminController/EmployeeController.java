package testthuctap.demo.Controller.AdminController;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import testthuctap.demo.Model.Attendance;
import testthuctap.demo.Model.User;
import testthuctap.demo.Model.WorkSchedule;
import testthuctap.demo.Service.AttendanceService;
import testthuctap.demo.Service.UserService;
import testthuctap.demo.Service.WorkScheduleService;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class EmployeeController {

    private final AttendanceService attendanceService;
    private final UserService userService;
    private final WorkScheduleService workScheduleService;

    private static final String UPLOAD_DIR = "F:\\Githup\\thuctap\\Test-thuc-tap\\demo\\src\\main\\resources\\static\\img";

    public EmployeeController(AttendanceService attendanceService, UserService userService, WorkScheduleService workScheduleService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.workScheduleService = workScheduleService;
    }

    @GetMapping("/Employee")
    public String employeeDashboard(Model model, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) return "redirect:/login";

        model.addAttribute("user", loggedInUser);
        model.addAttribute("attendanceHistory", attendanceService.getAttendanceByUserId(loggedInUser.getId()));
        return "Employee/acupuncture";
    }

    @GetMapping("/Profile")
    public String profile(Model model, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) return "redirect:/login";

        model.addAttribute("user", loggedInUser);
        return "Employee/profile";
    }

    @GetMapping("/Profile/Edit")
    public String editProfile(Model model, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) return "redirect:/login";

        model.addAttribute("user", loggedInUser);
        return "Employee/edit-profile";
    }

    @PostMapping("/Profile/Edit")
    public String updateProfile(
            @ModelAttribute("user") User updatedUser,
            @RequestParam(value = "avatarFile", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        }

        try {
            loggedInUser.setFullName(updatedUser.getFullName());
            loggedInUser.setEmail(updatedUser.getEmail());
            loggedInUser.setPhone(updatedUser.getPhone());
            loggedInUser.setAddress(updatedUser.getAddress());
            loggedInUser.setDateOfBirth(updatedUser.getDateOfBirth());
            loggedInUser.setUpdatedAt(LocalDateTime.now());

            if (file != null && !file.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String originalFileName = file.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                File destination = new File(uploadDir, fileName);

                file.transferTo(destination);
                loggedInUser.setAvatar("/img/" + fileName);
            }

            userService.updateUser(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/Profile";
    }

    @PostMapping("/Employee/check-in")
    @ResponseBody
    public ResponseEntity<?> checkIn(HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) return unauthorizedResponse();
    
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(user.getId());
    
        if (todayAttendance.isPresent() && todayAttendance.get().getCheckInTime() != null) {
            return ResponseEntity.badRequest().body("Bạn đã check-in hôm nay");
        }
    
        Attendance attendance = todayAttendance.orElseGet(() -> new Attendance());
        attendance.setUser(user);
        attendance.setDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now());
        attendance.setStatus("PRESENT");
        attendance.setCreatedAt(LocalDateTime.now());
    
        attendanceService.updateAttendance(attendance);
    
        return ResponseEntity.ok("Check-in thành công lúc " + attendance.getCheckInTime());
    }
    
    @PostMapping("/Employee/check-out")
    @ResponseBody
    public ResponseEntity<?> checkOut(HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) return unauthorizedResponse();
    
        Optional<Attendance> todayAttendanceOpt = attendanceService.getTodayAttendance(user.getId());
    
        if (todayAttendanceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Bạn chưa check-in hôm nay");
        }
    
        Attendance todayAttendance = todayAttendanceOpt.get();
        if (todayAttendance.getCheckOutTime() != null) {
            return ResponseEntity.badRequest().body("Bạn đã check-out hôm nay");
        }
    
        todayAttendance.setCheckOutTime(LocalTime.now());
        todayAttendance.setStatus(attendanceService.calculateAttendanceStatus(
            todayAttendance.getCheckInTime(),
            todayAttendance.getCheckOutTime()
        ));
    
        attendanceService.updateAttendance(todayAttendance);
    
        return ResponseEntity.ok("Check-out thành công lúc " + todayAttendance.getCheckOutTime());
    }
    

    @GetMapping("/employee/history")
    @ResponseBody
    public ResponseEntity<?> getAttendanceHistory(HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) return unauthorizedResponse();

        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(user.getId()));
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private ResponseEntity<String> unauthorizedResponse() {
        return ResponseEntity.status(401).body("Vui lòng đăng nhập");
    }
}