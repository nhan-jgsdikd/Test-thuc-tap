package testthuctap.demo.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reward_discipline")
public class RewardDiscipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rd_id")
    private String rdId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type")
    private String type;

    @Column(name = "reason")
    private String reason;

    @Column(name = "amount")
    private double amount;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}