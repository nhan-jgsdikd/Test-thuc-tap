package testthuctap.demo.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import testthuctap.demo.Model.Attendance;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceDAO extends JpaRepository<Attendance, Long> {

    // Tìm theo user ID với query method
    List<Attendance> findByUserId(Long userId);

    // Tìm theo user ID và ngày với @Query
    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.date = :date")
    Attendance findAttendanceByUserAndDate(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );

    List<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
}