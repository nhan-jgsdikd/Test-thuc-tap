package testthuctap.demo.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import testthuctap.demo.Model.Statistics;


public interface StatisticsDAO extends JpaRepository<Statistics, Long> {

}