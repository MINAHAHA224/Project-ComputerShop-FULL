package vn.javaweb.ComputerShop.controller.report;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderReportDto;
import vn.javaweb.ComputerShop.service.export.ExportExcelService;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/order/report")
public class OrderReportController {


    private final ExportExcelService exportExcelService;

    // Endpoint xem trước Excel cho Đơn hàng
    @GetMapping("/excel/preview")
    public String previewOrderExcel(
            @RequestParam(name = "startDate", required = false) String startDate, // yyyy-MM-dd
            @RequestParam(name = "endDate", required = false) String endDate,     // yyyy-MM-dd
            @RequestParam(name = "status", required = false) String status,
            HttpSession session,
            Model model) {

        InformationDTO currentUser = (InformationDTO) session.getAttribute("informationDTO");
        if (currentUser == null) {
            return "redirect:/admin/login";
        }

        try {
            List<OrderReportDto> listOrders = exportExcelService.getOrdersForReport(startDate, endDate, status);
            List<String> allOrderStatuses = exportExcelService.getAllOrderStatuses(); // Lấy danh sách trạng thái cho dropdown

            if (listOrders != null && !listOrders.isEmpty()) {
                model.addAttribute("listOrders", listOrders);
            } else {
                String filterMsg = "";
                if (StringUtils.hasText(startDate)) filterMsg += " từ " + startDate;
                if (StringUtils.hasText(endDate)) filterMsg += " đến " + endDate;
                if (StringUtils.hasText(status)) filterMsg += " với trạng thái '" + status + "'";
                model.addAttribute("message", "Không có dữ liệu đơn hàng" + (filterMsg.isEmpty() ? "" : filterMsg) + " để xem trước.");
            }
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("printDate", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            model.addAttribute("allOrderStatuses", allOrderStatuses);
            // Truyền lại các filter đã chọn để hiển thị trên form
            model.addAttribute("selectedStartDate", startDate);
            model.addAttribute("selectedEndDate", endDate);
            model.addAttribute("selectedStatus", status);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error_message", "Lỗi khi chuẩn bị dữ liệu xem trước đơn hàng: " + e.getMessage());
        }
        return "admin/order/orderExcelPreview"; // JSP cho xem trước đơn hàng
    }

    // Endpoint tải file Excel cho Đơn hàng
    @GetMapping("/excel/download")
    public void downloadOrderExcel(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "status", required = false) String status,
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
            List<OrderReportDto> listOrders = exportExcelService.getOrdersForReport(startDate, endDate, status);

            if (listOrders == null || listOrders.isEmpty()) {
                response.setContentType("text/html; charset=UTF-8");
                String filterMsg = "";
                if (StringUtils.hasText(startDate)) filterMsg += " từ " + startDate;
                if (StringUtils.hasText(endDate)) filterMsg += " đến " + endDate;
                if (StringUtils.hasText(status)) filterMsg += " với trạng thái \\'" + status.replace("'", "\\'") + "\\'";
                response.getWriter().println("<script>alert('Không có dữ liệu đơn hàng" + (filterMsg.isEmpty() ? "" : filterMsg) + " để xuất Excel.'); window.history.back();</script>");
                return;
            }
            exportExcelService.generateOrdersExcelReport(listOrders, currentUser, startDate, endDate, status, response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("<script>alert('Lỗi khi tạo file Excel đơn hàng: " + e.getMessage().replace("'", "\\'") + "'); window.history.back();</script>");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
