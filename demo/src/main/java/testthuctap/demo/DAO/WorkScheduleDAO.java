package testthuctap.demo.DAO;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import testthuctap.demo.Model.WorkSchedule;

public interface WorkScheduleDAO extends JpaRepository<WorkSchedule, Long> {

    List<WorkSchedule> findByUserId(Long userId);

}