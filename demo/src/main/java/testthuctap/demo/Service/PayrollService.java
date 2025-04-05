package testthuctap.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testthuctap.demo.DAO.PayrollDAO;
import testthuctap.demo.DAO.UserDAO;
import testthuctap.demo.Model.Payroll;
import testthuctap.demo.Model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

    private final PayrollDAO payrollDAO;
    private final UserDAO userDAO;

    @Autowired
    public PayrollService(PayrollDAO payrollDAO, UserDAO userDAO) {
        this.payrollDAO = payrollDAO;
        this.userDAO = userDAO;
    }

    @Transactional
    public Payroll createPayroll(Payroll payroll) {
        // Kiểm tra dữ liệu đầu vào
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }
        if (payroll.getUser() == null || payroll.getUser().getId() == null) {
            throw new IllegalArgumentException("User or user ID cannot be null");
        }
        if (payroll.getPayrollId() == null || payroll.getPayrollId().trim().isEmpty()) {
            throw new IllegalArgumentException("Payroll ID cannot be null or empty");
        }
        if (payroll.getMonth() == null || payroll.getMonth().trim().isEmpty()) {
            throw new IllegalArgumentException("Month cannot be null or empty");
        }

        // Kiểm tra xem user có tồn tại không
        User user = userDAO.findById(payroll.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + payroll.getUser().getId()));

        // Gán lại user từ database để đảm bảo tính toàn vẹn
        payroll.setUser(user);

        // Đảm bảo createdAt được gán nếu chưa có
        if (payroll.getCreatedAt() == null) {
            payroll.setCreatedAt(LocalDateTime.now());
        }

        // Tính totalSalary trước khi lưu (nếu chưa được tính ở controller)
        payroll.setTotalSalary(payroll.getBaseSalary() + payroll.getBonus() - payroll.getDeduction());

        // Lưu vào database
        return payrollDAO.save(payroll);
    }

    @Transactional
    public Payroll updatePayroll(Payroll payroll) {
        // Kiểm tra dữ liệu đầu vào
        if (payroll == null || payroll.getId() == null) {
            throw new IllegalArgumentException("Payroll or payroll ID cannot be null");
        }

        // Kiểm tra xem payroll có tồn tại không
        Optional<Payroll> existingPayrollOpt = payrollDAO.findById(payroll.getId());
        if (existingPayrollOpt.isEmpty()) {
            throw new IllegalArgumentException("Payroll not found with id: " + payroll.getId());
        }

        // Lấy bản ghi hiện tại từ database
        Payroll existingPayroll = existingPayrollOpt.get();

        // Cập nhật các trường cần thiết
        existingPayroll.setPayrollId(payroll.getPayrollId());
        existingPayroll.setMonth(payroll.getMonth());
        existingPayroll.setBaseSalary(payroll.getBaseSalary());
        existingPayroll.setBonus(payroll.getBonus());
        existingPayroll.setDeduction(payroll.getDeduction());
        existingPayroll.setStatus(payroll.getStatus());

        // Tính lại totalSalary
        existingPayroll.setTotalSalary(existingPayroll.getBaseSalary() + existingPayroll.getBonus() - existingPayroll.getDeduction());

        // Lưu vào database
        return payrollDAO.save(existingPayroll);
    }

    public List<Payroll> getPayrollByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return payrollDAO.findByUserId(userId);
    }

    public Optional<Payroll> getPayrollById(Long payrollId) {
        if (payrollId == null) {
            throw new IllegalArgumentException("Payroll ID cannot be null");
        }
        return payrollDAO.findById(payrollId);
    }

    public Optional<Payroll> getPayrollByUserIdAndMonth(Long userId, String month) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (month == null || month.trim().isEmpty()) {
            throw new IllegalArgumentException("Month cannot be null or empty");
        }
        Payroll payroll = payrollDAO.findByUserIdAndMonth(userId, month);
        return Optional.ofNullable(payroll);
    }
}