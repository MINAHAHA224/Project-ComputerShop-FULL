package vn.javaweb.ComputerShop.controller.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.javaweb.ComputerShop.domain.dto.response.CountElementDTO;
import vn.javaweb.ComputerShop.service.user.AdminService;
import vn.javaweb.ComputerShop.utils.ConstantVariable;
import vn.javaweb.ComputerShop.utils.SecurityUtils;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminPageController {

    private final AdminService adminService;
    @GetMapping("/admin")
    public String getDashboard(Model model) {
        log.info(ConstantVariable.ADMIN_PAGE + "Start getDashboard at {} " , SecurityUtils.currentTime);
        CountElementDTO result = this.adminService.handleCountElement();
        model.addAttribute("elementUser", result.getCountElementUser());
        model.addAttribute("elementProduct", result.getCountElementProduct());
        model.addAttribute("elementOrder", result.getCountElementOrder());
        log.info(ConstantVariable.ADMIN_PAGE + "Start getDashboard at {} " , SecurityUtils.currentTime);

        return "admin/dashboard/show";
    }

}
