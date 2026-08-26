# 🍱 Nhóm A - Trưa Nay Ăn Gì (MealChoice Platform)

> **Đồ Án Tốt Nghiệp Chuyên Sâu Java & Spring Framework - CodeGym**  
> Nền tảng kết nối ẩm thực trực tuyến (Online Food Ordering & Delivery Platform) áp dụng mô hình kiến trúc Monolith kết hợp giữa **Server-Side Rendering (Thymeleaf)** và **RESTful API (Spring Boot + JWT)**.

---

## 🌟 Giới Thiệu Tổng Quan & Đối Tượng Sử Dụng

Dự án **"Trưa nay ăn gì" (MealChoice)** giải quyết bài toán đặt món và giao thức ăn trưa công sở/học đường, kết nối 3 đối tượng người dùng:

1. **Khách hàng (Customer / User):** Khám phá món ngon, lọc món theo bữa ăn (`Breakfast`, `Lunch`, `Dinner`, `Café`), tìm kiếm món gần khu vực hoặc giảm giá sâu, giỏ hàng & đặt món với phí vận chuyển tính động theo số km thực tế.
2. **Nhà hàng / Đối tác quán ăn (Merchant):** Đăng ký mở quán, quản lý danh sách món ăn (ảnh đại diện, giá, thời gian chế biến, phí dịch vụ), quản lý đơn hàng theo vòng đời thời gian thực, xem báo cáo doanh thu & thực hiện đối soát tài chính định kỳ.
3. **Quản trị viên (Admin):** Phê duyệt / từ chối hồ sơ quán mới kèm gửi email thông báo, khóa/mở khóa Merchant vi phạm, quản lý danh sách đối tác vận chuyển và giám sát luồng tiền chiết khấu sàn.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Backend
- **Ngôn ngữ:** Java 17 LTS
- **Framework nền tảng:** Spring Boot 3.x / 4.x
  - **Spring Web / Spring MVC:** Xử lý HTTP Request & điều hướng View SSR.
  - **Spring Data JPA & Hibernate:** Ánh xạ thực thể ORM & tối ưu truy vấn dữ liệu.
  - **Spring Security 6 & JJWT (0.12.6):** Xác thực Role-based, JWT Filter kết hợp Cookie & Header.
  - **Spring Validation:** Jakarta Bean Validation dữ liệu DTO đầu vào.
  - **Spring Mail:** Gửi email kích hoạt tài khoản & thông báo trạng thái.
- **Build Tool:** Gradle 8.x+
- **Cơ sở dữ liệu:** MySQL 8.0+ / InnoDB / Charset `utf8mb4`

### Frontend & Giao Diện
- **Template Engine:** Thymeleaf + Thymeleaf Extras Spring Security 6.
- **UI Framework & Styling:** Bootstrap 5, Custom CSS, FontAwesome Icons.
- **Client Scripting:** Vanilla JavaScript (ES6+), Fetch API (AJAX), GeoLocation API.

### Tích Hợp Dịch Vụ Ngoài
- **HeiGIT / OpenRouteService API:** Định vị tọa độ địa chỉ (Geocoding) và tính cước vận chuyển theo cự ly kilômét thực tế (Driving-car distance).
- **Gmail SMTP Server:** Gửi email kích hoạt tài khoản.

---

## 📋 Danh Mục Tính Năng Theo Sprint (Trello Roadmap)

### 📌 Sprint 1: Khởi tạo, Định danh & Quản lý Hồ sơ
- **User:** Đăng ký tài khoản (gửi email xác nhận kèm token), đăng nhập hệ thống, kích hoạt tài khoản qua liên kết email, cập nhật thông tin cá nhân (họ tên, ngày sinh, giới tính), quản lý danh sách địa chỉ giao hàng (Thêm/Sửa/Xóa).
- **Merchant:** Đăng ký mở cửa hàng, cập nhật thông tin nhà hàng (tên, số điện thoại, ngân hàng, STK).
- **Admin:** Xem danh sách Merchant, xem chi tiết hồ sơ, duyệt/từ chối đăng ký kèm email lý do, khóa/mở khóa tài khoản Merchant vi phạm (kèm popup xác nhận & chặn đăng nhập).
- **Layout:** Menu top hiển thị động theo trạng thái đăng nhập, giỏ hàng, Footer hệ thống.

### 📌 Sprint 2: Quản lý Thực đơn, Trang Chủ & Chi Tiết Món
- **Merchant:** Thêm món ăn mới (bắt buộc $\ge 2$ ảnh, chọn địa chỉ chi nhánh, thời gian chế biến, phí dịch vụ, tag), cập nhật món ăn, xóa mềm món ăn (popup xác nhận), xem danh sách món ăn kèm lượt xem/lượt đặt, tìm kiếm món ăn theo tên.
- **Khách hàng / Trang chủ:** Banner slide danh mục, Quick search theo 4 nhóm bữa ăn (`Breakfast`, `Lunch`, `Dinner`, `Café`), hiển thị 8 món gợi ý gần bạn, hiển thị 8 món giảm giá nhiều nhất, trang xem chi tiết món ăn (thông tin cơ bản, danh sách ảnh, đặt hàng nhanh).

### 📌 Sprint 3: Giỏ hàng, Đặt hàng, Đối tác Vận chuyển, Thống kê & Đối soát
- **Khách hàng:** Quản lý giỏ hàng (tạo, sửa số lượng, xem giỏ), đặt hàng (tính phí ship động theo km, phí dịch vụ, áp mã giảm giá, ước tính thời gian giao hàng), xem danh sách và chi tiết đơn hàng, hủy đơn hàng (chỉ khi đơn chưa ở trạng thái Đang giao).
- **Merchant:** Quản lý đơn hàng (xem danh sách theo trạng thái, xem chi tiết đơn, hủy đơn khi ở trạng thái "Chờ nhận hàng", tiếp nhận đơn chuyển "Đang chuẩn bị" kèm hẹn giờ chế biến), thống kê doanh số (theo tuần/tháng/quý, theo món ăn, theo khách hàng, theo coupon), CRUD coupon giảm giá, đối soát doanh thu (tính chiết khấu sàn $0.001\%$/đơn hoặc $0.0005\%$/đơn với DT $\ge 200$tr), xác nhận hoặc khiếu nại đối soát, đăng ký đối tác thân thiết (DT $> 100$tr), yêu cầu rút tiền/thanh lý hợp đồng.
- **Admin:** Quản lý đối tác vận chuyển (tạo mới, xem chi tiết, cập nhật, danh sách, khóa/mở khóa), tính toán chiết khấu sàn cho các đơn hàng.

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### 1. Yêu Cầu Cài Đặt
- Java Development Kit (JDK) 17 trở lên.
- MySQL Server 8.0+.
- Gradle 8.x (hoặc sử dụng wrapper `gradlew` có sẵn trong dự án).

### 2. Cài Đặt Cơ Sở Dữ Liệu
1. Mở MySQL Workbench hoặc CLI và tạo Database:
   ```sql
   CREATE DATABASE meal_choice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Thực thi kịch bản tạo bảng và dữ liệu mẫu trong thư mục `database/`:
   - Chạy `database/schema.sql` (Tạo bảng, khóa chính, khóa ngoại).
   - Chạy `database/seed.sql` (Chèn dữ liệu mẫu danh mục, món ăn, tài khoản thử nghiệm).

### 3. Cấu Hình Ứng Dụng
Mở file `src/main/resources/application.properties` và chỉnh sửa các thông số:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/meal_choice?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password
```

### 4. Chạy Ứng Dụng
- **macOS / Linux:**
  ```bash
  ./gradlew bootRun
  ```
- **Windows:**
  ```cmd
  gradlew.bat bootRun
  ```
Truy cập website tại: **`http://localhost:8080`**

---

## 👥 Danh Sách Tài Khoản Mẫu (Test Accounts)

| Role | Email đăng nhập | Mật khẩu | Chức năng chính |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin@mealchoice.vn` | `123456` | Quản lý đối tác vận chuyển, duyệt/khóa Merchant |
| **Chủ cửa hàng (MERCHANT)** | `merchant1@gmail.com` | `123456` | Quản lý menu món ăn, xử lý đơn hàng, đối soát |
| **Chủ cửa hàng (MERCHANT)** | `merchant2@gmail.com` | `123456` | Quản lý quán trà sữa / ăn vặt |
| **Khách hàng (USER)** | `user1@gmail.com` | `123456` | Đặt hàng, theo dõi đơn, thích món |
| **Khách hàng (USER)** | `user2@gmail.com` | `123456` | Đặt hàng, quản lý địa chỉ giao hàng |

---

## 💡 Đóng Góp & Quy Chuẩn Phát Triển

Mọi đóng góp mã nguồn hoặc tính năng mới cần tuân thủ nghiêm ngặt các quy tắc trong [GEMINI.md](file:///Users/wanbi/Code/student-projects/MealChoice/GEMINI.md).
