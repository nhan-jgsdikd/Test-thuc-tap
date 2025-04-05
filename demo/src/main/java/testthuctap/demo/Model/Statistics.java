package testthuctap.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "statistics")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statistic_id")
    private String statisticId;

    @Column(name = "type")
    private String type;

    @Column(name = "month")
    private String month;

    @Column(name = "data")
    private String data;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}