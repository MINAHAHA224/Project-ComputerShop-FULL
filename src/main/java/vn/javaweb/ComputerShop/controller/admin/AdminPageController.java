package vn.javaweb.ComputerShop.controller.admin;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.javaweb.ComputerShop.domain.dto.response.CountElementDTO;
import vn.javaweb.ComputerShop.service.user.AdminService;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminService adminService;
    @GetMapping("/admin")
    public String getDashboard(Model model) {
        CountElementDTO result = this.adminService.handleCountElement();
        model.addAttribute("elementUser", result.getCountElementUser());
        model.addAttribute("elementProduct", result.getCountElementProduct());
        model.addAttribute("elementOrder", result.getCountElementOrder());
        return "admin/dashboard/show";
    }

}
