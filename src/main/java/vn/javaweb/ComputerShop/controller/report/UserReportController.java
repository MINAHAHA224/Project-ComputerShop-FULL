package vn.javaweb.ComputerShop.controller.report;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.response.RoleSimpleDto;
import vn.javaweb.ComputerShop.domain.dto.response.UserReportDto;
import vn.javaweb.ComputerShop.service.export.ExportExcelService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/user/report")
public class UserReportController {


    private final ExportExcelService exportExcelService;

    // Endpoint xem trước Excel cho User
    @GetMapping("/excel/preview")
    public String previewUserExcel(
            @RequestParam(name = "roleId", required = false) String roleId, // Lọc theo ID vai trò
            HttpSession session,
            Model model) {

        InformationDTO currentUser = (InformationDTO) session.getAttribute("informationDTO");
        if (currentUser == null) {
            return "redirect:/admin/login"; // Hoặc trang login của admin
        }

        try {
            List<UserReportDto> listUsers = exportExcelService.getUsersForReport(roleId);
            List<RoleSimpleDto> allRoles = exportExcelService.getAllRoles(); // Để hiển thị lại dropdown

            String selectedRoleName = null;
            if (roleId != null && !roleId.isEmpty() && allRoles != null) {
                try {
                    Long rId = Long.parseLong(roleId);
                    Optional<RoleSimpleDto> roleOpt = allRoles.stream().filter(r -> r.getId().equals(rId)).findFirst();
                    if (roleOpt.isPresent()) {
                        selectedRoleName = roleOpt.get().getName();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid roleId format for fetching name: " + roleId);
                }
            }


            if (listUsers != null && !listUsers.isEmpty()) {
                model.addAttribute("listUsers", listUsers);
                model.addAttribute("printDate", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                model.addAttribute("selectedRoleId", roleId);
                model.addAttribute("selectedRoleName", selectedRoleName); // Truyền tên vai trò đã chọn
            } else {
                String filterMessage = "";
                if (selectedRoleName != null) {
                    filterMessage = " cho vai trò '" + selectedRoleName + "'";
                } else if (roleId != null && !roleId.isEmpty()) {
                    filterMessage = " cho bộ lọc vai trò hiện tại";
                }
                model.addAttribute("message", "Không có dữ liệu người dùng" + filterMessage + " để xem trước.");
            }
            model.addAttribute("currentUser", currentUser); // Thông tin người dùng hiện tại
            model.addAttribute("allRoles", allRoles); // Danh sách các vai trò
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error_message", "Lỗi khi chuẩn bị dữ liệu xem trước: " + e.getMessage());
        }
        return "admin/user/userExcelPreview"; // JSP cho xem trước user
    }

    // Endpoint tải file Excel cho User
    @GetMapping("/excel/download")
    public void downloadUserExcel(
            @RequestParam(name = "roleId", required = false) String roleId,
            HttpSession session,
            HttpServletResponse response) {

        InformationDTO currentUser = (InformationDTO) session.getAttribute("informationDTO");
        if (currentUser == null) {
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            List<UserReportDto> listUsers = exportExcelService.getUsersForReport(roleId);
            String selectedRoleName = null;
            if (roleId != null && !roleId.isEmpty()) {
                List<RoleSimpleDto> allRoles = exportExcelService.getAllRoles();
                try {
                    Long rId = Long.parseLong(roleId);
                    Optional<RoleSimpleDto> roleOpt = allRoles.stream().filter(r -> r.getId().equals(rId)).findFirst();
                    if (roleOpt.isPresent()) {
                        selectedRoleName = roleOpt.get().getName();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid roleId format for fetching name (download): " + roleId);
                }
            }


            if (listUsers == null || listUsers.isEmpty()) {
                response.setContentType("text/html; charset=UTF-8");
                String filterMessage = "";
                if (selectedRoleName != null) {
                    filterMessage = " cho vai trò \\'" + selectedRoleName.replace("'", "\\'") + "\\'";
                } else if (roleId != null && !roleId.isEmpty()) {
                    filterMessage = " cho bộ lọc vai trò hiện tại";
                }
                response.getWriter().println("<script>alert('Không có dữ liệu người dùng" + filterMessage + " để xuất Excel.'); window.history.back();</script>");
                return;
            }
            exportExcelService.generateUsersExcelReport(listUsers, currentUser, selectedRoleName, response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("<script>alert('Lỗi khi tạo file Excel: " + e.getMessage().replace("'", "\\'") + "'); window.history.back();</script>");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}