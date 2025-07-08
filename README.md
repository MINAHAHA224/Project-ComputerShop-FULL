<div align="center">
  <img src="https://img.icons8.com/plasticine/200/laptop.png" alt="logo" width="120" height="120">
  <h1 align="center">Website Bán Laptop Trực Tuyến "3TLap"</h1>
  <p align="center">
    Một dự án website thương mại điện tử hoàn chỉnh xây dựng bằng Java Spring Boot và JavaScript, tích hợp Chatbot AI hỗ trợ tư vấn thông minh.
  </p>
</div>

<div align="center">
  <a href="#-giới-thiệu-đề-tài">Giới thiệu</a> •
  <a href="#-công-nghệ-sử-dụng">Công nghệ</a> •
  <a href="#-tính-năng-nổi-bật">Tính năng</a> •
  <a href="#-hướng-dẫn-cài-đặt">Cài đặt</a> •
  <a href="#-thành-viên-nhóm">Tác giả</a>
</div>

---

## 📖 Giới thiệu Đề tài

**3TLap** là một dự án ứng dụng web được xây dựng cho môn học Lập trình Web, mô phỏng một hệ thống thương mại điện tử chuyên kinh doanh các sản phẩm laptop. Dự án không chỉ dừng lại ở các chức năng e-commerce cơ bản mà còn tích hợp một **trợ lý ảo Chatbot AI** nhằm nâng cao trải nghiệm người dùng, cung cấp hỗ trợ tư vấn sản phẩm 24/7.

**Mục tiêu chính của dự án:**

-   **Xây dựng nền tảng E-commerce vững chắc:** Cung cấp đầy đủ các chức năng từ xem sản phẩm, quản lý giỏ hàng, đặt hàng đến thanh toán trực tuyến.
-   **Tạo giao diện quản trị mạnh mẽ:** Cho phép admin dễ dàng quản lý toàn bộ hệ thống.
-   **Nâng cao trải nghiệm người dùng:** Tích hợp Chatbot AI để tư vấn, giải đáp thắc mắc và hỗ trợ khách hàng một cách thông minh, tức thì.
-   **Áp dụng công nghệ hiện đại:** Vận dụng các công nghệ, framework phổ biến trong ngành như Spring Boot, Spring Security, JPA/Hibernate, và các thư viện JavaScript.

---

## 🚀 Công Nghệ Sử Dụng

<p align="center">
  <a href="https://www.java.com" target="_blank"> 
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original-wordmark.svg" alt="java" width="50" height="50"/> 
  </a> 
  <a href="https://spring.io/" target="_blank"> 
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/spring/spring-original-wordmark.svg" alt="spring" width="50" height="50"/> 
  </a> 
  <a href="https://www.mysql.com/" target="_blank"> 
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/mysql/mysql-original-wordmark.svg" alt="mysql" width="50" height="50"/> 
  </a>
  <a href="https://www.javascript.com" target="_blank">
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/javascript/javascript-original.svg" alt="javascript" width="40" height="40"/>
  </a>
  <a href="https://www.w3.org/html/" target="_blank">
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/html5/html5-original-wordmark.svg" alt="html5" width="40" height="40"/>
  </a>
  <a href="https://www.w3schools.com/css/" target="_blank">
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/css3/css3-original-wordmark.svg" alt="css3" width="40" height="40"/>
  </a>
   <a href="https://getbootstrap.com" target="_blank" rel="noreferrer">
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/bootstrap/bootstrap-original-wordmark.svg" alt="bootstrap" width="50" height="50"/>
  </a>
  <a href="https://jquery.com/" target="_blank"> 
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/jquery/jquery-original-wordmark.svg" alt="jquery" width="50" height="50"/> 
  </a>
  <a href="https://maven.apache.org/" target="_blank"> 
    <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/maven/maven-original-wordmark.svg" alt="maven" width="50" height="50"/> 
  </a> 
  <a href="https://www.postman.com" target="_blank" rel="noreferrer">
    <img src="https://www.vectorlogo.zone/logos/getpostman/getpostman-icon.svg" alt="postman" width="40" height="40"/>
  </a>
</p>

### **Backend**

-   **Ngôn ngữ & Framework:** Java 17, Spring Boot 3.2.2 (MVC, Security, Data JPA, Session JDBC).
-   **Cơ sở dữ liệu:** MySQL.
-   **Ngôn ngữ truy vấn:** SQL.
-   **ORM:** Hibernate.
-   **Xác thực:** Spring Security (Form Login, Google OAuth2).
-   **Tích hợp:**
    -   **Thanh toán:** Momo Payment Gateway.
    -   **Email:** Spring Mail (JavaMailSender) gửi OTP và hóa đơn.
    -   **Đa ngôn ngữ (i18n):** Hỗ trợ Tiếng Việt & Tiếng Anh thông qua Spring i18n (MessageSource).
    -   **Báo cáo:** Apache POI (Xuất file Excel , Báo cáo , Hóa Đơn).
-   **Build & Deploy:** Maven, Embedded Tomcat.

### **Frontend(Client-side & View Layer)**

-   **Ngôn ngữ & Công nghệ View:** JSP, JSTL, HTML5, CSS3, JavaScript (ES6+).
-   **Thư viện & Framework UI:** Bootstrap 5, jQuery.
-   **Tương tác & Hiệu ứng:** Owl Carousel, Lightbox, WOW.js.
-   **Giao diện Quản trị (Admin UI):** Chart.js (cho biểu đồ) và SimpleDataTables (cho bảng dữ liệu tương tác).
-   **Tương tác API (Client-side):**
    -   **Goong Maps API:** Gợi ý và tự động hoàn thành địa chỉ.
    -   **Chatbot API:** Giao tiếp với Trợ lý ảo AI để tư vấn và hỗ trợ.


### ** Chatbot AI Service **

-   **Nền tảng Backend (Giả định):** Python (Flask/FastAPI).
-   **Cơ sở kiến thức:** Typesense Search Engine.
-   **Xử lý NLP:** Các kỹ thuật nhận dạng ý định (Intent Recognition) và trích xuất thực thể (Entity Extraction).
-   **API:** Cung cấp RESTful API cho Frontend.

---

## ✨ Tính Năng Nổi Bật

### **👤 Dành cho Khách Hàng (Client)**

| Tính Năng                    | Mô Tả Chi Tiết                                                                                                                                                             |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **🔐 Quản lý Tài khoản**      | Đăng ký, Đăng nhập (thường & Google), Đổi mật khẩu/avatar, Quản lý thông tin cá nhân.                                                                                       |
| **🔑 Quên Mật Khẩu**         | Quy trình bảo mật lấy lại mật khẩu qua mã **OTP** gửi về email.                                                                                                            |
| **💻 Tìm kiếm & Lọc Sản phẩm** | Xem danh sách sản phẩm, **phân trang**, **lọc** đa tiêu chí (hãng, giá, mục đích), và **sắp xếp** (giá tăng/giảm dần).                                                      |
| **🛒 Giỏ hàng Động**          | Thêm, xóa, **cập nhật số lượng sản phẩm và tự động tính lại tổng tiền** mà không cần tải lại trang.                                                                          |
| **💳 Thanh toán Đa dạng**     | Hỗ trợ 2 phương thức thanh toán phổ biến: **Thanh toán khi nhận hàng (COD)** và thanh toán trực tuyến qua **Cổng thanh toán Momo**.                                          |
| **🗺️ Gợi ý Địa chỉ Thông minh** | Tích hợp **Goong API** để tự động gợi ý và hoàn thành địa chỉ giao hàng, giảm sai sót và tăng tốc độ đặt hàng.                                                            |
| **📜 Lịch sử Mua hàng**        | Xem lại toàn bộ lịch sử các đơn hàng đã đặt, theo dõi trạng thái (Chờ xác nhận, Đang giao, Đã giao,...) và xem chi tiết từng đơn.                                          |
| **🤖 Chatbot AI Hỗ trợ 24/7**   | Trợ lý ảo thông minh giúp **tư vấn sản phẩm**, trả lời **FAQ**, cung cấp thông tin chính sách, và **gợi ý các câu hỏi liên quan**.                                           |
| **🌐 Đa ngôn ngữ (i18n)**     | Giao diện hỗ trợ **Tiếng Việt** và **Tiếng Anh**, cho phép người dùng chuyển đổi dễ dàng.                                                                                     |

### **⚙️ Dành cho Quản Trị Viên (Admin)**

| Tính Năng                     | Mô Tả Chi Tiết                                                                                                                                                                                                                              |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **📊 Dashboard Trực quan**      | Bảng điều khiển tổng quan với các thẻ thống kê nhanh (số người dùng, sản phẩm, đơn hàng) và **biểu đồ** (doanh thu, sản phẩm bán chạy) được vẽ bằng **Chart.js**.                                                                             |
| **👥 Quản lý Người dùng (CRUD)** | Quản lý toàn diện thông tin người dùng: Xem danh sách (với **SimpleDataTables**), tạo mới, xem chi tiết, cập nhật thông tin và phân quyền.                                                                                                 |
| **💻 Quản lý Sản phẩm (CRUD)**  | Quản lý toàn bộ vòng đời sản phẩm: Thêm sản phẩm mới (upload ảnh), xem chi tiết, cập nhật thông tin và xóa sản phẩm.                                                                                                                       |
| **📦 Quản lý Đơn hàng**         | Xem danh sách đơn hàng, xem chi tiết từng đơn, và **cập nhật trạng thái đơn hàng** (với stepper trực quan) cũng như trạng thái thanh toán.                                                                                                |
| **📄 Xuất Báo cáo Excel**       | Tính năng mạnh mẽ cho phép **xem trước dữ liệu** và **xuất báo cáo** danh sách người dùng, sản phẩm, đơn hàng ra file **Excel (.xlsx)** với các tùy chọn **lọc dữ liệu** theo tiêu chí (vai trò, hãng, ngày tháng,...).                      |
| **🎨 Giao diện tùy chỉnh**       | Giao diện quản trị được tùy chỉnh từ theme SB Admin, hỗ trợ **chế độ Sáng/Tối (Dark Mode)** giúp quản trị viên làm việc hiệu quả.                                                                                                           |

---

## 🛠 Hướng Dẫn Cài Đặt

1.  **Prerequisites:**
    *   Java JDK 17 hoặc cao hơn.
    *   Apache Maven.
    *   MySQL Server.
    *   Một IDE cho Java (ví dụ: IntelliJ IDEA, Eclipse).

2.  **Cấu hình Backend:**
    *   Clone repository về máy:
        ```bash
        git clone [URL_CỦA_REPOSITORY]
        ```
    *   Tạo một cơ sở dữ liệu trong MySQL (ví dụ: `laptopshop`).
    *   Import file database script (`database.sql` - *bạn cần tạo file này*) vào database vừa tạo để có cấu trúc bảng và dữ liệu mẫu.
    *   Mở file `src/main/resources/application.properties`:
        *   Cập nhật thông tin kết nối CSDL của bạn (`spring.datasource.url`, `username`, `password`).
        *   Cấu hình các API key và thông tin khác (Gmail, Google OAuth2, Momo) nếu bạn muốn thử nghiệm đầy đủ các tính năng.
    *   Mở dự án bằng IDE và chạy file `ComputerShopApplication.java` để khởi động server backend.

3.  **Truy cập ứng dụng:**
    *   **Trang Client:** Mở trình duyệt và truy cập `http://localhost:8080`.
    *   **Trang Admin:** Truy cập `http://localhost:8080/admin` và đăng nhập bằng tài khoản admin.
    *   **Chatbot AI Service:** Đảm bảo service chatbot đang chạy (nếu là một project riêng).

---

