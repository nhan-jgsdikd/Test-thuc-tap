package testthuctap.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "users") // Loại bỏ List<User> khỏi toString()
@Table(name = "position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_id")
    private String positionId;

    @Column(name = "position_name", nullable = false)
    private String positionName;

    @Column(name = "description")
    private String description;

    @Column(name = "base_salary")
    private double baseSalary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Mối quan hệ ngược (One-to-Many)
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    private List<User> users;
}