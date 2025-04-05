package testthuctap.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testthuctap.demo.DAO.WorkScheduleDAO;
import testthuctap.demo.Model.User;
import testthuctap.demo.Model.WorkSchedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkScheduleService {

    @Autowired
    private WorkScheduleDAO workScheduleRepository;

    @Autowired
    private UserService userService;

    // Lấy tất cả WorkSchedule
    public List<WorkSchedule> getAllWorkSchedules() {
        return workScheduleRepository.findAll();
    }

    // Lấy WorkSchedule theo userId
    public List<WorkSchedule> getWorkSchedulesByUserId(Long userId) {
        return workScheduleRepository.findAll().stream()
                .filter(ws -> ws.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    // Lưu WorkSchedule
    public WorkSchedule saveWorkSchedule(WorkSchedule workSchedule) {
        return workScheduleRepository.save(workSchedule);
    }

    // Xóa WorkSchedule
    public void deleteWorkSchedule(Long id) {
        workScheduleRepository.deleteById(id);
    }

    // Xử lý check-in
    public WorkSchedule checkIn(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + userId));

        LocalDate today = LocalDate.now();
        Optional<WorkSchedule> existingCheckIn = workScheduleRepository.findAll().stream()
                .filter(ws -> ws.getUser().getId().equals(userId)
                        && ws.getDate().equals(today)
                        && ws.getCheckInTime() != null)
                .findFirst();

        if (existingCheckIn.isPresent()) {
            throw new IllegalStateException("Nhân viên đã check-in trong ngày hôm nay!");
        }

        WorkSchedule workSchedule = WorkSchedule.builder()
                .user(user)
                .date(today)
                .checkInTime(LocalTime.now())
                .status("CHECKED_IN")
                .createdAt(LocalDateTime.now())
                .totalHours(0.0) // Giá trị mặc định
                .build();

        return workScheduleRepository.save(workSchedule);
    }

    // Xử lý check-out
    public WorkSchedule checkOut(Long workScheduleId) {
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi WorkSchedule với ID: " + workScheduleId));

        if (workSchedule.getCheckInTime() == null) {
            throw new IllegalStateException("Nhân viên chưa check-in!");
        }
        if (workSchedule.getCheckOutTime() != null) {
            throw new IllegalStateException("Nhân viên đã check-out trước đó!");
        }

        LocalTime checkOutTime = LocalTime.now();
        workSchedule.setCheckOutTime(checkOutTime);
        workSchedule.setStatus("CHECKED_OUT");

        Duration duration = Duration.between(workSchedule.getCheckInTime(), checkOutTime);
        Double totalHours = duration.toMinutes() / 60.0; // Chuyển đổi sang giờ (Double)
        workSchedule.setTotalHours(totalHours);

        return workScheduleRepository.save(workSchedule);
    }

    // Tìm WorkSchedule theo userId và date
    public Optional<WorkSchedule> findByUserAndDate(Long userId, LocalDate date) {
        return workScheduleRepository.findAll().stream()
                .filter(ws -> ws.getUser().getId().equals(userId) && ws.getDate().equals(date))
                .findFirst();
    }

    // Lấy tổng giờ làm việc của một WorkSchedule
    public Double getTotalHours(Long workScheduleId) {
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi WorkSchedule với ID: " + workScheduleId));
        return workSchedule.getTotalHours() != null ? workSchedule.getTotalHours() : 0.0;
    }

    // Lấy tổng giờ làm việc theo userId và tháng
    public double getTotalHoursByUserAndMonth(Long userId, String month) {
        try {
            String formattedMonth;
            // Xử lý định dạng MM/YYYY thành YYYY-MM
            if (month.matches("\\d{2}/\\d{4}")) { // Kiểm tra định dạng MM/YYYY
                String[] parts = month.split("/");
                formattedMonth = parts[1] + "-" + parts[0]; // Chuyển thành YYYY-MM
            } else if (month.matches("\\d{4}-\\d{2}")) { // Đã ở định dạng YYYY-MM
                formattedMonth = month;
            } else {
                throw new IllegalArgumentException("Định dạng tháng không hợp lệ: " + month + ". Mong đợi MM/YYYY hoặc YYYY-MM");
            }

            // Tạo ngày đầu và cuối tháng
            LocalDate startOfMonth = LocalDate.parse(formattedMonth + "-01"); // Thêm "-01" để tạo YYYY-MM-DD
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            List<WorkSchedule> schedules = workScheduleRepository.findAll().stream()
                    .filter(ws -> ws.getUser().getId().equals(userId)
                            && !ws.getDate().isBefore(startOfMonth)
                            && !ws.getDate().isAfter(endOfMonth)
                            && ws.getTotalHours() != null)
                    .collect(Collectors.toList());

            return schedules.stream()
                    .mapToDouble(WorkSchedule::getTotalHours)
                    .sum();
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi xử lý tháng: " + month + ". Mong đợi MM/YYYY hoặc YYYY-MM", e);
        }
    }
}