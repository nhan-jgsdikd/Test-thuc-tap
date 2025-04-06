package testthuctap.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testthuctap.demo.DAO.UserDAO;
import testthuctap.demo.Model.User;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Lấy tất cả nhân viên (role = EMPLOYEE)
    public List<User> getAllEmployees() {
        return userDAO.findByRole("EMPLOYEE");
    }

    // Lưu hoặc cập nhật user
    public void saveUser(User user) {
        userDAO.save(user);
    }

    // Xóa user theo ID
    public void deleteUser(Long id) {
        userDAO.deleteById(id);
    }

    // Lấy user theo ID
    public Optional<User> getUserById(Long id) {
        return userDAO.findById(id);
    }

    // Cập nhật thông tin user (giữ nguyên để tương thích)
    public void updateUser(User user) {
        userDAO.save(user);
    }

    // Lấy tất cả user
    public List<User> getAllUsers() {
        return userDAO.findAll();  // Assuming UserDAO extends JpaRepository
    }

    public User getCurrentUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentUser'");
    }
}
