package testthuctap.demo.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import testthuctap.demo.Model.Payroll;

import java.util.List;

public interface PayrollDAO extends JpaRepository<Payroll, Long> {
    List<Payroll> findByUserId(Long userId);
    Payroll findByUserIdAndMonth(Long userId, String month); // Sửa từ Object thành Payroll
}