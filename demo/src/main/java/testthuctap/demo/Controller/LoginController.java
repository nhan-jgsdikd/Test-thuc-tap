package testthuctap.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Sửa import từ ch.qos.logback.core.model.Model thành org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import testthuctap.demo.DAO.UserDAO;
import testthuctap.demo.Model.User;

@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về file login.html trong thư mục templates
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, 
                               @RequestParam String password,
                               Model model, 
                               HttpSession session) {
        User user = userDAO.findByEmail(email); 

        if (user == null) {
            model.addAttribute("error", "Tên đăng nhập không tồn tại!");
            return "login"; 
        }

        if (!password.equals(user.getPassword())) {
            model.addAttribute("error", "Mật khẩu không chính xác!");
            return "login"; 
        }

        session.setAttribute("loggedInUser", user);

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/Admin"; 
        } else {
            return "redirect:/Employee"; 
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa session người dùng
        return "redirect:/login";
    }
}