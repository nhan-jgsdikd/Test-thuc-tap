package testthuctap.demo.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import testthuctap.demo.Model.Position;

public interface PositionDAO extends JpaRepository<Position, Long> {

}