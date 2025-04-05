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
    public Attendance createAttendance(Long userId, LocalDate date, LocalTime checkInTime, LocalTime checkOutTime) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Validate đầu vào
        if (date == null || checkInTime == null) {
            throw new IllegalArgumentException("Date and check-in time cannot be null");
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .date(date)
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .status(calculateAttendanceStatus(checkInTime, checkOutTime))
                .note(null) // Có thể để trống hoặc thêm logic để ghi chú
                .createdAt(LocalDateTime.now())
                .build();

        return attendanceDAO.save(attendance);
    }

    @Transactional
    public Attendance updateAttendance(Attendance attendance) {
        if (attendance == null || attendance.getId() == null) {
            throw new IllegalArgumentException("Attendance or attendance ID cannot be null");
        }

        // Cập nhật trạng thái khi sửa giờ vào/ra
        attendance.setStatus(calculateAttendanceStatus(attendance.getCheckInTime(), attendance.getCheckOutTime()));
        return attendanceDAO.save(attendance);
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
        if (checkInTime.isAfter(startTime)) {
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