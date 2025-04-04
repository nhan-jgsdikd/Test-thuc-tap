package testthuctap.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.model.Model;


@Controller
public class IndexController {
    
    @GetMapping("/Employee")
    public String Employee(Model Model) {
        return  "Employee";
    }
    @GetMapping("/Admin")
    public String Admin(Model Model) {
        return  "Admin";
    }
}
