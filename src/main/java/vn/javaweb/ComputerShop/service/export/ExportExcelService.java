package vn.javaweb.ComputerShop.service.export;

import jakarta.servlet.http.HttpServletResponse;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderReportDto;
import vn.javaweb.ComputerShop.domain.dto.response.ProductReportDto;
import vn.javaweb.ComputerShop.domain.dto.response.RoleSimpleDto;
import vn.javaweb.ComputerShop.domain.dto.response.UserReportDto;

import java.io.IOException;
import java.util.List;

 public interface ExportExcelService {
     List<ProductReportDto> getProductsForReport(String factory);
     List<String> getAllFactories();
     void generateProductsExcelReport(List<ProductReportDto> products, InformationDTO informationDTO, String selectedFactory, HttpServletResponse response) throws IOException;


     List<UserReportDto> getUsersForReport(String roleIdFilter);
     List<RoleSimpleDto> getAllRoles();
     void generateUsersExcelReport(List<UserReportDto> users, InformationDTO currentUser, String selectedRoleName, HttpServletResponse response) throws IOException ;


     List<String> getAllOrderStatuses();
     List<OrderReportDto> getOrdersForReport(String startDateStr, String endDateStr, String statusFilter);
     void generateOrdersExcelReport(List<OrderReportDto> orders, InformationDTO currentUser,
                                          String startDate, String endDate, String selectedStatus,
                                          HttpServletResponse response) throws IOException;
}
