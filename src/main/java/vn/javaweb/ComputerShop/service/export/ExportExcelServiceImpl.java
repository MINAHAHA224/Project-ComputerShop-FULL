package vn.javaweb.ComputerShop.service.export;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderReportDto;
import vn.javaweb.ComputerShop.domain.dto.response.ProductReportDto;
import vn.javaweb.ComputerShop.domain.dto.response.RoleSimpleDto;
import vn.javaweb.ComputerShop.domain.dto.response.UserReportDto;
import vn.javaweb.ComputerShop.domain.enums.OrderStatus;
import vn.javaweb.ComputerShop.utils.ConstantVariable;
import vn.javaweb.ComputerShop.utils.StoreProcedureConstance;

import java.sql.Timestamp; // Thêm import này
import java.text.ParseException; // Thêm import này

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExportExcelServiceImpl implements ExportExcelService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProductReportDto> getProductsForReport(String factory) {
        List<ProductReportDto> result = new ArrayList<>();
        /* Apply StoreProcedure for feature excel
               try {
        Query query = query.CreateNativeQuery(StoreProcedureConstance.Export_Data_Product);
        query.setParameter(1, factory);
        List<Object[]> listResult = query.getResultList();
        result = listResult.stream().map(rs ->
                    ProductReportDto.builder()
                            .id(Long.valueOf(rs[0].toString()))
                            .name(rs[1].toString())
                            .price((Double) rs[2])
                            .shortDesc(rs[3].toString())
                            .quantity(Long.valueOf(rs[4].toString()))
                            .sold(Long.valueOf(rs[5].toString()))
                            .build()
            ).collect(Collectors.toList());
            } catch (RuntimeException e ){
            log.warn("--ERROR getProductsForReport: {} ", e.getMessage());
            System.err.println("--ERROR getProductsForReport: " + e.getMessage());
            }

         */
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT id, name, price, short_desc, quantity, sold " +
                        "FROM products USE INDEX (IX_FACTORY_PRODUCTS , IX_NAME_PRODUCTS)"
        );
        boolean hasFactoryFilter = factory != null && !factory.trim().isEmpty();

        if (hasFactoryFilter) {
            sqlBuilder.append(" WHERE factory ='").append(factory).append("'");
        }
        sqlBuilder.append(" ORDER BY name ASC"); // Sắp xếp theo tên cho dễ nhìn

        try {
            Query query = entityManager.createNativeQuery(sqlBuilder.toString());
            List<Object[]> listResult = query.getResultList();
            result = listResult.stream().map(rs ->
                    ProductReportDto.builder()
                            .id(Long.valueOf(rs[0].toString()))
                            .name(rs[1].toString())
                            .price((Double) rs[2])
                            .shortDesc(rs[3].toString())
                            .quantity(Long.valueOf(rs[4].toString()))
                            .sold(Long.valueOf(rs[5].toString()))
                            .build()
            ).collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.warn("--ERROR getProductsForReport: {} ", e.getMessage());
            System.err.println("--ERROR getProductsForReport: " + e.getMessage());
        }
        return result;
    }

    @Override
    public List<String> getAllFactories() {
        List<String> factories = new ArrayList<>();
        String sql = "SELECT DISTINCT factory " +
                "FROM products USE INDEX (IX_FACTORY_PRODUCTS)  " +
                "WHERE factory IS NOT NULL AND factory <> '' ORDER BY factory ASC";
        try {
            Query query = entityManager.createNativeQuery(sql);
            factories = query.getResultList();


        } catch (RuntimeException e) {
            log.error(ConstantVariable.ERROR_MES + "getAllFactories : {}", e.getMessage());
            System.err.println("--ERROR getAllFactories: " + e.getMessage());
        }
        return factories;
    }

    @Override
    public void generateProductsExcelReport(List<ProductReportDto> products, InformationDTO informationDTO, String selectedFactory, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh Sách Sản Phẩm");

        // --- Font Styles ---
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);

        Font tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.BLUE.getIndex());

        // --- Cell Styles ---
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerInfoStyle = workbook.createCellStyle();
        headerInfoStyle.setFont(headerFont);
        headerInfoStyle.setAlignment(HorizontalAlignment.LEFT);

        CellStyle tableHeaderStyle = workbook.createCellStyle();
        tableHeaderStyle.setFont(tableHeaderFont);
        tableHeaderStyle.setBorderTop(BorderStyle.THIN);
        tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
        tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
        tableHeaderStyle.setBorderRight(BorderStyle.THIN);
        tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        tableHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setWrapText(true); // Cho phép xuống dòng nếu text dài

        CellStyle numberCellStyle = workbook.createCellStyle();
        numberCellStyle.cloneStyleFrom(dataCellStyle);
        numberCellStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle currencyCellStyle = workbook.createCellStyle();
        currencyCellStyle.cloneStyleFrom(dataCellStyle);
        currencyCellStyle.setAlignment(HorizontalAlignment.RIGHT);
        CreationHelper createHelper = workbook.getCreationHelper();
        currencyCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0")); // Định dạng số tiền

        // --- Report Title ---
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        String reportTitle = "DANH SÁCH SẢN PHẨM";
        if (selectedFactory != null && !selectedFactory.trim().isEmpty()) {
            reportTitle += " (HÃNG: " + selectedFactory.toUpperCase() + ")";
        }
        titleCell.setCellValue(reportTitle);
        titleCell.setCellStyle(titleStyle);
        // Số cột: STT, ID, Tên, Giá, Mô tả ngắn, SL, Đã bán (7 cột)
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // --- Report Info ---
        Row staffRow = sheet.createRow(2);
        Cell staffLabelCell = staffRow.createCell(0);
        staffLabelCell.setCellValue("Người lập báo cáo:");
        Cell staffValueCell = staffRow.createCell(1);
        staffValueCell.setCellValue(informationDTO != null ? informationDTO.getFullName() : "N/A"); // Giả sử LoginDto có getUsername()
        staffLabelCell.setCellStyle(headerInfoStyle);
        staffValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 1, 3));

        Row dateRow = sheet.createRow(3);
        Cell dateLabelCell = dateRow.createCell(0);
        dateLabelCell.setCellValue("Ngày in:");
        Cell dateValueCell = dateRow.createCell(1);
        dateValueCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        dateLabelCell.setCellStyle(headerInfoStyle);
        dateValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 1, 3));

        // --- Table Header ---
        Row headerDataRow = sheet.createRow(5);
        String[] columns = {"STT", "Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Mô Tả Ngắn", "Tồn Kho", "Đã Bán"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerDataRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(tableHeaderStyle);
        }

        // --- Table Data ---
        int rowNum = 6;
        int stt = 1;
        for (ProductReportDto product : products) {
            Row row = sheet.createRow(rowNum++);

            Cell sttCell = row.createCell(0);
            sttCell.setCellValue(stt++);
            sttCell.setCellStyle(numberCellStyle); // Căn phải cho STT

            Cell idCell = row.createCell(1);
            idCell.setCellValue(product.getId());
            idCell.setCellStyle(numberCellStyle); // Căn phải cho ID

            Cell nameCell = row.createCell(2);
            nameCell.setCellValue(product.getName());
            nameCell.setCellStyle(dataCellStyle);

            Cell priceCell = row.createCell(3);
            if (product.getPrice() != null) {
                priceCell.setCellValue(product.getPrice());
            }
            priceCell.setCellStyle(currencyCellStyle);

            Cell shortDescCell = row.createCell(4);
            shortDescCell.setCellValue(product.getShortDesc());
            shortDescCell.setCellStyle(dataCellStyle);

            Cell quantityCell = row.createCell(5);
            if (product.getQuantity() != null) {
                quantityCell.setCellValue(product.getQuantity());
            }
            quantityCell.setCellStyle(numberCellStyle);

            Cell soldCell = row.createCell(6);
            if (product.getSold() != null) {
                soldCell.setCellValue(product.getSold());
            }
            soldCell.setCellStyle(numberCellStyle);
        }

        // --- Auto-size columns ---
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        // Điều chỉnh chiều rộng cột cụ thể nếu cần
        sheet.setColumnWidth(2, 35 * 256); // Tên Sản Phẩm
        sheet.setColumnWidth(4, 40 * 256); // Mô tả ngắn

        // --- Write output to HttpServletResponse ---
        String filename = "DanhSachSanPham";
        if (selectedFactory != null && !selectedFactory.trim().isEmpty()) {
            filename += "_" + selectedFactory.replaceAll("[^a-zA-Z0-9]", "_"); // Thay thế ký tự đặc biệt để tên file hợp lệ
        }
        filename += "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @Override
    public List<UserReportDto> getUsersForReport(String roleIdFilter) {
        List<UserReportDto> result = new ArrayList<>();
//        StringBuilder sqlBuilder = new StringBuilder(
//                "SELECT u.id, u.full_name, u.email, u.phone, u.address, r.name as role_name, " +
//                        "(SELECT GROUP_CONCAT(am.login_type SEPARATOR ', ') FROM auth_method am WHERE am.user_id = u.id) as auth_methods " +
//                        "FROM users u " +
//                        "LEFT JOIN roles r ON u.role_id = r.id"
//        );
//
//        boolean hasRoleFilter = roleIdFilter != null && !roleIdFilter.trim().isEmpty();
//        if (hasRoleFilter) {
//            sqlBuilder.append(" WHERE u.role_id = :roleId");
//        }
//        sqlBuilder.append(" ORDER BY u.full_name ASC, u.id ASC");

        // SQL Optimize
        StringBuilder sqlBuilder = new StringBuilder(
                "WITH role_option AS (\n" +
                        "\tSELECT r.id , r.name\n" +
                        "\tFROM roles r\n" +
                        "\t),\n" +
                        "\tauth_option AS (\n" +
                        "\tSELECT au.id , au.user_id , au.login_type\n" +
                        "\tFROM auth_method au\n" +
                        "\t)\n" +
                        "\tSELECT u.id , u.full_name AS fullName , u.email , u.phone , u.address , ro.name  AS role_name , ao.login_type  AS auth_methods\n" +
                        "\tFROM users u USE INDEX (IX_FULLNAME_ID_USERNAME)\n" +
                        "\tINNER JOIN role_option ro ON ro.id =  u.role_id\n" +
                        "\tLEFT JOIN auth_option ao ON  u.id = ao.user_id "
        );

        boolean hasRoleFilter = roleIdFilter != null && !roleIdFilter.trim().isEmpty();
        if (hasRoleFilter) {
            sqlBuilder.append(" WHERE (:roleId IS NULL OR u.role_id = :roleId  ) ");
        }
        sqlBuilder.append(" ORDER BY u.full_name , u.id ");

        try {
            Query query = entityManager.createNativeQuery(sqlBuilder.toString());
            if (hasRoleFilter) {
                try {
                    query.setParameter("roleId", Long.parseLong(roleIdFilter));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid roleIdFilter format: " + roleIdFilter);

                    query = entityManager.createNativeQuery(
                            "WITH role_option AS (\n" +
                                    "\tSELECT r.id , r.name\n" +
                                    "\tFROM roles r\n" +
                                    "\t),\n" +
                                    "\tauth_option AS (\n" +
                                    "\tSELECT au.id , au.user_id , au.login_type\n" +
                                    "\tFROM auth_method au\n" +
                                    "\t)\n" +
                                    "\tSELECT u.id , u.full_name AS fullName , u.email , u.phone , u.address , ro.name  AS role_name , ao.login_type  AS auth_methods\n" +
                                    "\tFROM users u USE INDEX (IX_FULLNAME_ID_USERNAME)\n" +
                                    "\tINNER JOIN role_option ro ON ro.id =  u.role_id\n" +
                                    "\tLEFT JOIN auth_option ao ON  u.id = ao.user_id "
                    );
                }
            }

            List<Object[]> listResult = query.getResultList();
            for (Object[] rs : listResult) {
                UserReportDto user = new UserReportDto();
                user.setId(rs[0] != null ? ((Number) rs[0]).longValue() : null);
                user.setFullName(rs[1] != null ? rs[1].toString() : null);
                user.setEmail(rs[2] != null ? rs[2].toString() : null);
                user.setPhone(rs[3] != null ? rs[3].toString() : null);
                user.setAddress(rs[4] != null ? rs[4].toString() : null);
                user.setRoleName(rs[5] != null ? rs[5].toString() : "N/A");
                user.setAuthMethods(rs[6] != null ? rs[6].toString() : "N/A");
                result.add(user);
            }
        } catch (Exception e) {
            System.err.println("--ERROR getUsersForReport: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy danh sách tất cả các vai trò (ID và Name).
     */
    @Override
    public List<RoleSimpleDto> getAllRoles() {
        List<RoleSimpleDto> roles = new ArrayList<>();
        String sql = "SELECT r.id, r.name FROM roles r ORDER BY r.name ASC";
        try {
            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> listResult = query.getResultList();
            for (Object[] rs : listResult) {
                Long id = Long.valueOf(rs[0].toString());
                String name = (String) rs[1];
                roles.add(new RoleSimpleDto(id, name));
            }
        } catch (Exception e) {
            System.err.println("--ERROR getAllRoles: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Tạo file Excel báo cáo danh sách người dùng.
     */
    @Override
    public void generateUsersExcelReport(List<UserReportDto> users, InformationDTO currentUser, String selectedRoleName, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh Sách Người Dùng");

        // --- Font Styles (Tương tự như product report) ---
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        Font tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.BLACK.getIndex());

        // --- Cell Styles (Tương tự như product report) ---
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        CellStyle headerInfoStyle = workbook.createCellStyle();
        headerInfoStyle.setFont(headerFont);
        headerInfoStyle.setAlignment(HorizontalAlignment.LEFT);
        CellStyle tableHeaderStyle = workbook.createCellStyle();
        tableHeaderStyle.setFont(tableHeaderFont);
        tableHeaderStyle.setBorderTop(BorderStyle.THIN);
        tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
        tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
        tableHeaderStyle.setBorderRight(BorderStyle.THIN);
        tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        tableHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setWrapText(true);
        CellStyle centerDataCellStyle = workbook.createCellStyle();
        centerDataCellStyle.cloneStyleFrom(dataCellStyle);
        centerDataCellStyle.setAlignment(HorizontalAlignment.CENTER);


        // --- Report Title ---
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        String reportTitleStr = "DANH SÁCH NGƯỜI DÙNG";
        if (selectedRoleName != null && !selectedRoleName.trim().isEmpty()) {
            reportTitleStr += " (VAI TRÒ: " + selectedRoleName.toUpperCase() + ")";
        }
        titleCell.setCellValue(reportTitleStr);
        titleCell.setCellStyle(titleStyle);
        // Số cột: STT, Mã ND, Họ tên, Email, SĐT, Địa chỉ, Vai trò, P.Thức Login (8 cột)
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

        // --- Report Info ---
        Row staffRow = sheet.createRow(2);
        Cell staffLabelCell = staffRow.createCell(0);
        staffLabelCell.setCellValue("Người lập báo cáo:");
        Cell staffValueCell = staffRow.createCell(1);
        // Giả sử InformationDTO có getFullName() hoặc getEmail()
        staffValueCell.setCellValue(currentUser != null ? (currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getEmail()) : "N/A");
        staffLabelCell.setCellStyle(headerInfoStyle);
        staffValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 1, 3));

        Row dateRow = sheet.createRow(3);
        Cell dateLabelCell = dateRow.createCell(0);
        dateLabelCell.setCellValue("Ngày in:");
        Cell dateValueCell = dateRow.createCell(1);
        dateValueCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        dateLabelCell.setCellStyle(headerInfoStyle);
        dateValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 1, 3));

        // --- Table Header ---
        Row headerDataRow = sheet.createRow(5);
        String[] columns = {"STT", "Mã ND", "Họ và Tên", "Email", "Số Điện Thoại", "Địa Chỉ", "Vai Trò", "P.Thức Login"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerDataRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(tableHeaderStyle);
        }

        // --- Table Data ---
        int rowNum = 6;
        int stt = 1;
        for (UserReportDto user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.getCell(0).setCellStyle(centerDataCellStyle);
            row.createCell(1).setCellValue(user.getId());
            row.getCell(1).setCellStyle(centerDataCellStyle);
            row.createCell(2).setCellValue(user.getFullName());
            row.getCell(2).setCellStyle(dataCellStyle);
            row.createCell(3).setCellValue(user.getEmail());
            row.getCell(3).setCellStyle(dataCellStyle);
            row.createCell(4).setCellValue(user.getPhone());
            row.getCell(4).setCellStyle(dataCellStyle);
            row.createCell(5).setCellValue(user.getAddress());
            row.getCell(5).setCellStyle(dataCellStyle);
            row.createCell(6).setCellValue(user.getRoleName());
            row.getCell(6).setCellStyle(dataCellStyle);
            row.createCell(7).setCellValue(user.getAuthMethods());
            row.getCell(7).setCellStyle(dataCellStyle);
        }

        // --- Auto-size columns ---
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(2, 25 * 256); // Họ tên
        sheet.setColumnWidth(3, 30 * 256); // Email
        sheet.setColumnWidth(5, 35 * 256); // Địa chỉ

        // --- Write output to HttpServletResponse ---
        String filename = "DanhSachNguoiDung";
        if (selectedRoleName != null && !selectedRoleName.trim().isEmpty()) {
            filename += "_" + selectedRoleName.replaceAll("[^a-zA-Z0-9]", "_");
        }
        filename += "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public List<String> getAllOrderStatuses() {
        List<String> statuses = new ArrayList<>();
        String sql = "SELECT DISTINCT o.status FROM orders o WHERE o.status IS NOT NULL AND o.status <> '' ORDER BY o.status ASC";
        Map<String, String> orderStatus = OrderStatus.getOrderStatusMap();
        for (Map.Entry<String, String> status : orderStatus.entrySet()) {
            statuses.add(status.getKey());
        }

        return statuses;
    }

    /**
     * Lấy danh sách đơn hàng để báo cáo, có thể lọc theo khoảng thời gian và trạng thái.
     *
     * @param startDateStr Ngày bắt đầu (yyyy-MM-dd).
     * @param endDateStr   Ngày kết thúc (yyyy-MM-dd).
     * @param statusFilter Trạng thái đơn hàng để lọc.
     * @return Danh sách OrderReportDto.
     */
    @Override
    public List<OrderReportDto> getOrdersForReport(String startDateStr, String endDateStr, String statusFilter) {
        List<OrderReportDto> result = new ArrayList<>();

//        StringBuilder sqlBuilder = new StringBuilder(
//                "SELECT " +
//                        "o.id AS order_id, o.time AS order_time, o.total_price, " +
//                        "o.receiver_name, o.receiver_phone, o.receiver_address, " +
//                        "o.status AS order_status, o.type_payment, o.status_payment, " +
//                        "u.full_name AS customer_name, u.email AS customer_email, " +
//                        "(SELECT GROUP_CONCAT(CONCAT(p.name, ' (SL: ', od.quantity, ')') SEPARATOR '; ') " +
//                        " FROM order_detail od JOIN products p ON od.product_id = p.id " +
//                        " WHERE od.order_id = o.id) AS product_details " +
//                        "FROM orders o " +
//                        "LEFT JOIN users u ON o.user_id = u.id "
//        );

        // SQL Optimize
        StringBuilder sqlBuilder = new StringBuilder(
                "WITH customer_orders AS (\n" +
                        " SELECT u.id, u.full_name , u.email\n" +
                        " FROM users u\n" +
                        "),\n" +
                        "orderDetai_orders AS (\n" +
                        "SELECT  od.order_id , GROUP_CONCAT( CONCAT ( pr.name , ' ( SL : ' , od.quantity , ') ' ) SEPARATOR  '| ') AS pr_detail  \n" +
                        "FROM order_detail od\n" +
                        "INNER JOIN (SELECT id , name FROM  products ) AS pr ON pr.id = od.product_id\n" +
                        "GROUP BY od.order_id \n" +
                        ")\n" +
                        " SELECT o.id order_id , o.time order_time , o.total_price , o.receiver_name , o.receiver_phone , o.receiver_address,\n" +
                        " o.status order_status , o.type_payment , o.status_payment , odo.pr_detail AS product_details\n" +
                        " FROM orders o USE INDEX (IX_TIME_ID_ORDERS) \n" +
                        " INNER JOIN customer_orders co ON co.id = o.user_id\n" +
                        " INNER JOIN orderDetai_orders odo ON odo.order_id = o.id"
        );

        // B1 : nếu có nhiều hơn 2 điều kiện => sử dụng List để lưu conditions
        // B2 : Validation các condition (null hay là có dữ liệu , nếu mà có thì đưa về chuẩn định dạng ,... )
        // B3 : Nếu có conditions thì add vô StringBuilder  , và add các OderBy
        // B4 : Chuyển StringBuilder về String nếu có condition thì setParameter cho nó
        List<String> conditions = new ArrayList<>();
        boolean hasStartDate = StringUtils.hasText(startDateStr);
        boolean hasEndDate = StringUtils.hasText(endDateStr);
        boolean hasStatusFilter = StringUtils.hasText(statusFilter);

        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            if (hasStartDate) {
                startDate = sdf.parse(startDateStr);
                conditions.add("o.time >= :startDate");
            }
            if (hasEndDate) {
                // Để bao gồm cả ngày kết thúc, ta cần lấy đến cuối ngày đó , bởi vì nhiều DB nó lưu ngày cuối sẽ là
                // 2025-23-07 00:00:00 -> những order của ngày 2025-23-07 23:05:00 ,... đều ko được lấy -> sai
                // => phải cộng thêm 1 ngày để lấy < hơn (2025-24-07 00:00:00) -> 2025-23-07 23:05:00 ,... sẽ lấy được -> đúng

                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(endDateStr));
                c.add(Calendar.DAY_OF_MONTH, 1); // Chuyển sang ngày hôm sau
                endDate = c.getTime();
                conditions.add("o.time < :endDate"); // Sử dụng '<' với ngày hôm sau
            }
        } catch (ParseException e) {
            System.err.println("Error parsing date for order report: " + e.getMessage());
            // Có thể quyết định không lọc theo ngày nếu parse lỗi hoặc throw exception
            hasStartDate = false;
            hasEndDate = false;
        }


        if (hasStatusFilter) {
            conditions.add("o.status = :status");
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", conditions));
        }
        sqlBuilder.append(" ORDER BY o.time , o.id ");

        try {
            Query query = entityManager.createNativeQuery(sqlBuilder.toString());
            if (hasStartDate && startDate != null) {
                query.setParameter("startDate", new Timestamp(startDate.getTime()));
            }
            if (hasEndDate && endDate != null) {
                query.setParameter("endDate", new Timestamp(endDate.getTime()));
            }
            if (hasStatusFilter) {
                query.setParameter("status", statusFilter);
            }

            List<Object[]> listResult = query.getResultList();
            for (Object[] rs : listResult) {
                OrderReportDto order = new OrderReportDto();
                order.setId(rs[0] != null ? ((Number) rs[0]).longValue() : null);
                order.setOrderTime(rs[1] != null ? (Date) rs[1] : null); // DB time/datetime map trực tiếp sang Date
                order.setTotalPrice(rs[2] != null ? ((Number) rs[2]).doubleValue() : null);
                order.setReceiverName(rs[3] != null ? rs[3].toString() : null);
                order.setReceiverPhone(rs[4] != null ? rs[4].toString() : null);
                order.setReceiverAddress(rs[5] != null ? rs[5].toString() : null);
                order.setOrderStatus(rs[6] != null ? rs[6].toString() : null);
                order.setPaymentType(rs[7] != null ? rs[7].toString() : null);
                order.setPaymentStatus(rs[8] != null ? rs[8].toString() : null);
                order.setCustomerFullName(rs[9] != null ? rs[9].toString() : "Khách vãng lai"); // Hoặc để null
                order.setCustomerEmail(rs[10] != null ? rs[10].toString() : null);
                order.setProductDetails(rs[11] != null ? rs[11].toString() : "Không có chi tiết");
                result.add(order);
            }
        } catch (Exception e) {
            System.err.println("--ERROR getOrdersForReport: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Tạo file Excel báo cáo danh sách đơn hàng.
     */
    @Override
    public void generateOrdersExcelReport(List<OrderReportDto> orders, InformationDTO currentUser,
                                          String startDate, String endDate, String selectedStatus,
                                          HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh Sách Đơn Hàng");
        SimpleDateFormat dateFormatForExcel = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // --- Font Styles (Tương tự như các report khác) ---
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        Font tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.BLACK.getIndex());

        // --- Cell Styles (Tương tự) ---
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        CellStyle headerInfoStyle = workbook.createCellStyle();
        headerInfoStyle.setFont(headerFont);
        headerInfoStyle.setAlignment(HorizontalAlignment.LEFT);
        CellStyle tableHeaderStyle = workbook.createCellStyle();
        tableHeaderStyle.setFont(tableHeaderFont);
        tableHeaderStyle.setBorderTop(BorderStyle.THIN);
        tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
        tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
        tableHeaderStyle.setBorderRight(BorderStyle.THIN);
        tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        tableHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setWrapText(true);
        CellStyle centerDataCellStyle = workbook.createCellStyle();
        centerDataCellStyle.cloneStyleFrom(dataCellStyle);
        centerDataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        CellStyle numberCellStyle = workbook.createCellStyle();
        numberCellStyle.cloneStyleFrom(dataCellStyle);
        numberCellStyle.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle currencyCellStyle = workbook.createCellStyle();
        currencyCellStyle.cloneStyleFrom(numberCellStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        currencyCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0"));
        CellStyle dateTimeCellStyle = workbook.createCellStyle();
        dateTimeCellStyle.cloneStyleFrom(dataCellStyle);
        dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));


        // --- Report Title ---
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        String reportTitleStr = "DANH SÁCH ĐƠN HÀNG";
        List<String> filterDescriptions = new ArrayList<>();
        if (StringUtils.hasText(startDate)) filterDescriptions.add("Từ: " + startDate);
        if (StringUtils.hasText(endDate)) filterDescriptions.add("Đến: " + endDate);
        if (StringUtils.hasText(selectedStatus)) filterDescriptions.add("Trạng thái: " + selectedStatus);
        if (!filterDescriptions.isEmpty()) {
            reportTitleStr += " (" + String.join(", ", filterDescriptions) + ")";
        }
        titleCell.setCellValue(reportTitleStr);
        titleCell.setCellStyle(titleStyle);
        // Số cột: STT, Mã ĐH, Ngày Đặt, Khách Hàng, Email KH, Người Nhận, SĐT Nhận, Địa Chỉ, Tổng Tiền, TT ĐH, HTTT, TT TT, Chi Tiết SP (13 cột)
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 12));

        // --- Report Info (Người lập, Ngày in) ---
        Row staffRow = sheet.createRow(2);
        Cell staffLabelCell = staffRow.createCell(0);
        staffLabelCell.setCellValue("Người lập báo cáo:");
        Cell staffValueCell = staffRow.createCell(1);
        staffValueCell.setCellValue(currentUser != null ? (currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getEmail()) : "N/A");
        staffLabelCell.setCellStyle(headerInfoStyle);
        staffValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 1, 3));
        Row dateRow = sheet.createRow(3);
        Cell dateLabelCell = dateRow.createCell(0);
        dateLabelCell.setCellValue("Ngày in:");
        Cell dateValueCell = dateRow.createCell(1);
        dateValueCell.setCellValue(dateFormatForExcel.format(new Date()));
        dateLabelCell.setCellStyle(headerInfoStyle);
        dateValueCell.setCellStyle(headerInfoStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 1, 3));

        // --- Table Header ---
        Row headerDataRow = sheet.createRow(5);
        String[] columns = {"STT", "Mã ĐH", "Ngày Đặt", "Khách Hàng", "Email KH", "Người Nhận", "SĐT Nhận", "Địa Chỉ Giao", "Tổng Tiền", "TT ĐH", "HT Thanh Toán", "TT Thanh Toán", "Chi Tiết Sản Phẩm"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerDataRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(tableHeaderStyle);
        }

        // --- Table Data ---
        int rowNum = 6;
        int stt = 1;
        for (OrderReportDto order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.getCell(0).setCellStyle(centerDataCellStyle);
            row.createCell(1).setCellValue(order.getId());
            row.getCell(1).setCellStyle(centerDataCellStyle);

            Cell orderTimeCell = row.createCell(2);
            if (order.getOrderTime() != null) orderTimeCell.setCellValue(order.getOrderTime());
            orderTimeCell.setCellStyle(dateTimeCellStyle);

            row.createCell(3).setCellValue(order.getCustomerFullName());
            row.getCell(3).setCellStyle(dataCellStyle);
            row.createCell(4).setCellValue(order.getCustomerEmail());
            row.getCell(4).setCellStyle(dataCellStyle);
            row.createCell(5).setCellValue(order.getReceiverName());
            row.getCell(5).setCellStyle(dataCellStyle);
            row.createCell(6).setCellValue(order.getReceiverPhone());
            row.getCell(6).setCellStyle(dataCellStyle);
            row.createCell(7).setCellValue(order.getReceiverAddress());
            row.getCell(7).setCellStyle(dataCellStyle);

            Cell totalPriceCell = row.createCell(8);
            if (order.getTotalPrice() != null) totalPriceCell.setCellValue(order.getTotalPrice());
            totalPriceCell.setCellStyle(currencyCellStyle);

            row.createCell(9).setCellValue(order.getOrderStatus());
            row.getCell(9).setCellStyle(dataCellStyle);
            row.createCell(10).setCellValue(order.getPaymentType());
            row.getCell(10).setCellStyle(dataCellStyle);
            row.createCell(11).setCellValue(order.getPaymentStatus());
            row.getCell(11).setCellStyle(dataCellStyle);
            row.createCell(12).setCellValue(order.getProductDetails());
            row.getCell(12).setCellStyle(dataCellStyle);
        }

        // --- Auto-size columns ---
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        // Điều chỉnh chiều rộng cột cụ thể nếu cần
        sheet.setColumnWidth(3, 25 * 256); // Khách Hàng
        sheet.setColumnWidth(4, 30 * 256); // Email KH
        sheet.setColumnWidth(7, 40 * 256); // Địa Chỉ Giao
        sheet.setColumnWidth(12, 50 * 256); // Chi Tiết Sản Phẩm

        // --- Write output to HttpServletResponse ---
        String filename = "DanhSachDonHang";
        // Thêm bộ lọc vào tên file nếu có
        if (StringUtils.hasText(startDate)) filename += "_Tu" + startDate.replace("-", "");
        if (StringUtils.hasText(endDate)) filename += "_Den" + endDate.replace("-", "");
        if (StringUtils.hasText(selectedStatus)) filename += "_" + selectedStatus.replaceAll("[^a-zA-Z0-9]", "");

        filename += "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
