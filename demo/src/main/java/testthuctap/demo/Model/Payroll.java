package testthuctap.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payroll_id")
    private String payrollId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month")
    private String month;

    @Column(name = "base_salary")
    private double baseSalary;

    @Column(name = "bonus")
    private double bonus;

    @Column(name = "deduction")
    private double deduction;

    @Column(name = "total_salary")
    private double totalSalary;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}