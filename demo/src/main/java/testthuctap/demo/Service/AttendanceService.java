package testthuctap.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testthuctap.demo.DAO.AttendanceDAO;
import testthuctap.demo.DAO.UserDAO;
import testthuctap.demo.Model.Attendance;
import testthuctap.demo.Model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;
    private final UserDAO userDAO;

    @Autowired
    public AttendanceService(AttendanceDAO attendanceDAO, UserDAO userDAO) {
        this.attendanceDAO = attendanceDAO;
        this.userDAO = userDAO;
    }

    @Transactional
    public Attendance createAttendance(Attendance attendance) {
        if (attendance == null || attendance.getUser() == null || attendance.getDate() == null) {
            throw new IllegalArgumentException("Attendance, user, or date cannot be null");
        }

        // Đảm bảo user tồn tại trong DB
        User user = userDAO.findById(attendance.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + attendance.getUser().getId()));

        attendance.setUser(user);
        attendance.setStatus(calculateAttendanceStatus(attendance.getCheckInTime(), attendance.getCheckOutTime()));
        if (attendance.getCreatedAt() == null) {
            attendance.setCreatedAt(LocalDateTime.now());
        }

        return attendanceDAO.save(attendance);
    }

    @Transactional
    public Attendance updateAttendance(Attendance attendance) {
        if (attendance == null || attendance.getId() == null) {
            throw new IllegalArgumentException("Attendance or attendance ID cannot be null");
        }

        // Đảm bảo bản ghi tồn tại trước khi cập nhật
        Attendance existingAttendance = attendanceDAO.findById(attendance.getId())
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found with id: " + attendance.getId()));

        // Cập nhật các trường cần thiết
        existingAttendance.setCheckInTime(attendance.getCheckInTime());
        existingAttendance.setCheckOutTime(attendance.getCheckOutTime());
        existingAttendance.setStatus(calculateAttendanceStatus(attendance.getCheckInTime(), attendance.getCheckOutTime()));
        existingAttendance.setNote(attendance.getNote());
        existingAttendance.setCreatedAt(attendance.getCreatedAt()); // Giữ nguyên createdAt từ bản ghi cũ nếu không thay đổi
        existingAttendance.setUser(attendance.getUser());

        return attendanceDAO.save(existingAttendance);
    }

    public List<Attendance> getAttendanceByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return attendanceDAO.findByUserId(userId);
    }

    public Optional<Attendance> getAttendanceById(Long attendanceId) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("Attendance ID cannot be null");
        }
        return attendanceDAO.findById(attendanceId);
    }

    public Optional<Attendance> getTodayAttendance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return Optional.ofNullable(attendanceDAO.findAttendanceByUserAndDate(userId, LocalDate.now()));
    }

    public List<Attendance> getAttendanceByUserIdAndDate(Long userId, LocalDate date) {
        if (userId == null || date == null) {
            throw new IllegalArgumentException("User ID and date cannot be null");
        }
        return attendanceDAO.findByUserIdAndDate(userId, date);
    }

    public String calculateAttendanceStatus(LocalTime checkInTime, LocalTime checkOutTime) {
        // Thời gian làm việc tiêu chuẩn (8:00 - 17:00)
        LocalTime startTime = LocalTime.of(8, 0);  // Giờ bắt đầu làm việc
        LocalTime endTime = LocalTime.of(17, 0);   // Giờ kết thúc làm việc

        String status = "Đúng giờ";

        // Kiểm tra giờ vào
        if (checkInTime != null && checkInTime.isAfter(startTime)) {
            status = "Muộn";
        }

        // Kiểm tra giờ ra nếu có
        if (checkOutTime != null) {
            if (checkOutTime.isBefore(endTime)) {
                status += " - Về sớm";
            } else if (status.equals("Đúng giờ")) {
                status = "Hoàn thành đủ giờ";
            }
        } else if (status.equals("Đúng giờ")) {
            status = "Chưa chấm giờ ra";
        }

        return status;
    }
}