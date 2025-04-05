package testthuctap.demo.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import testthuctap.demo.Model.User;

public interface UserDAO extends JpaRepository<User, Long> {

    User findByEmail(String email);

    List<User> findByRole(String string);
}