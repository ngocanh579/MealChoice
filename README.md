# 🍱 MealChoice - Nền Tảng Đặt & Giao Đồ Ăn Trực Tuyến

> **Dự án thực hành chuyên sâu môn Java & Spring Framework - CodeGym**  
> Ứng dụng web thương mại điện tử giao đồ ăn trực tuyến (Food Ordering & Delivery Platform) xây dựng với mô hình Monolith hiện đại, kết hợp giữa **Server-Side Rendering (Thymeleaf)** và **RESTful API (Spring Boot + JWT)**.

---

## 🌟 Giới Thiệu Tổng Quan

**MealChoice** kết nối 3 đối tượng người dùng chính trong hệ sinh thái F&B:
1. **Khách hàng (Customer / User):** Khám phá món ngon, tìm kiếm theo khoảng cách/địa chỉ, đặt hàng với phí ship tính toán tự động qua API bản đồ thực tế.
2. **Chủ quán / Nhà hàng (Merchant):** Đăng ký mở quán, quản lý thực đơn nhiều hình ảnh, phân loại món, quản lý và xử lý đơn hàng theo thời gian thực.
3. **Quản trị viên (Admin):** Phê duyệt hoặc khóa quán ăn vi phạm, quản lý các đối tác vận chuyển (Delivery Partners).

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Backend
- **Ngôn ngữ:** Java 17 (LTS)
- **Framework nền tảng:** Spring Boot 3.x / 4.x
  - **Spring Web / Spring MVC:** Xử lý HTTP Request & điều hướng View.
  - **Spring Data JPA & Hibernate:** Quản lý ánh xạ ORM & cơ sở dữ liệu quan hệ.
  - **Spring Security 6 & JJWT (0.12.6):** Xác thực phân quyền Role-based, JWT Filter kết hợp Cookie & Header.
  - **Spring Validation:** Jakarta Bean Validation dữ liệu đầu vào.
  - **Spring Mail:** Gửi email kích hoạt tài khoản bằng mã Token bảo mật.
- **Build Tool:** Gradle 8.x+
- **Cơ sở dữ liệu:** MySQL 8.0+ / InnoDB / Charset `utf8mb4`

### Frontend & Giao Diện
- **Template Engine:** Thymeleaf + Thymeleaf Extras Spring Security 6.
- **UI Framework & Styling:** Bootstrap 5, Custom CSS, Icons (FontAwesome / Bootstrap Icons).
- **Client Scripting:** Vanilla JavaScript (ES6+), Fetch API (AJAX), GeoLocation API.

### Dịch Vụ Tích Hợp Bên Ngoài
- **OpenRouteService / HeiGIT API:** Định vị tọa độ địa chỉ (Geocoding) và tính cước vận chuyển theo khoảng cách thực tế (Driving-car distance).
- **Gmail SMTP Server:** Gửi email kích hoạt tài khoản.

---

## 📂 Cấu Trúc Thư Mục Dự Án (Project Architecture)

Dự án áp dụng mô hình phân lớp tiêu chuẩn **Layered Architecture (MVC)**:

```
MealChoice/
├── src/main/java/vn/codegyme/meal_choice/
│   ├── config/             # Cấu hình Spring (Security, Mail, RestTemplate, Web, DataSeeder)
│   ├── controller/         # Web Controller (Thymeleaf) & REST Controllers (API)
│   ├── dto/                # Data Transfer Objects (Request/Response DTOs & Validation)
│   ├── entity/             # Các JPA Entities ánh xạ bảng cơ sở dữ liệu
│   ├── event/              # Các sự kiện ứng dụng (UserRegisteredEvent, v.v.)
│   ├── exception/          # Xử lý ngoại lệ tập trung (GlobalExceptionHandler, ErrorResponse)
│   ├── mapper/             # Ánh xạ Entity <-> DTO (OrderMapper, v.v.)
│   ├── repository/         # Spring Data JPA Repositories (Derived queries, JPQL, EntityGraph)
│   ├── security/           # JWT Filter, CustomUserDetails, MerchantBlockedFilter
│   ├── service/            # Interface & Class nghiệp vụ chính
│   │   └── impl/           # Các lớp cài đặt (Service Implementations)
│   ├── util/               # Tiện ích bổ trợ (AddressNormalizer, v.v.)
│   └── MealChoiceApplication.java  # Main Application Entry Point
├── src/main/resources/
│   ├── static/             # Tài nguyên tĩnh (css, js, images, uploads)
│   ├── templates/          # Giao diện Thymeleaf (auth, user, merchant, admin, checkout, food)
│   └── application.properties # Cấu hình môi trường, DB, Mail, JWT
├── database/               # Kịch bản cơ sở dữ liệu (schema.sql, seed.sql)
├── build.gradle            # Khai báo thư viện & dependencies Gradle
├── GEMINI.md               # Quy tắc và chuẩn phát triển dành cho AI / Developer
└── README.md               # Tài liệu hướng dẫn sử dụng dự án
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 1. Yêu Cầu Môi Trường
- **JDK:** Java 17 hoặc mới hơn.
- **MySQL Server:** Phiên bản 8.0+.
- **IDE Khuyên dùng:** IntelliJ IDEA, Eclipse hoặc VS Code (kèm Extension Java).

### 2. Cài Đặt Cơ Sở Dữ Liệu MySQL
1. Khởi động MySQL Server.
2. Tạo cơ sở dữ liệu `meal_choice`:
   ```sql
   CREATE DATABASE meal_choice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Chạy file khởi tạo cấu trúc và dữ liệu mẫu (nằm trong thư mục `database/`):
   - Chạy file `database/schema.sql` (Tạo bảng & khóa ngoại).
   - Chạy file `database/seed.sql` (Thêm dữ liệu mẫu danh mục, món ăn, tài khoản test).

### 3. Cấu Hình `application.properties`
Mở file `src/main/resources/application.properties` và điều chỉnh các thông số phù hợp với máy của bạn:

```properties
# Cấu hình kết nối MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/meal_choice?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Cấu hình Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Cấu hình Gửi Mail (Gmail App Password)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password

# Cấu hình JWT & API bản đồ (nếu cần đổi)
jwt.secret=6afb00b4e213a2559d33b640c84e6c027595b9e36faea41f9dc458be9eebd467
```

### 4. Biên Dịch & Chạy Ứng Dụng
Sử dụng terminal tại thư mục gốc của dự án:

- **Trên macOS / Linux:**
  ```bash
  ./gradlew bootRun
  ```
- **Trên Windows:**
  ```cmd
  gradlew.bat bootRun
  ```

Sau khi ứng dụng khởi động thành công, mở trình duyệt truy cập: **`http://localhost:8080`**

---

## 👥 Danh Sách Tài Khoản Thử Nghiệm (Seed Accounts)

| Vai trò (Role) | Email đăng nhập | Mật khẩu mặc định | Quyền hạn chính |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin@mealchoice.vn` | `123456` | Quản lý Merchant, Quản lý đơn vị giao hàng |
| **Chủ cửa hàng (MERCHANT)** | `merchant1@gmail.com` | `123456` | Quản lý quán ăn, món ăn, xử lý đơn hàng |
| **Chủ cửa hàng (MERCHANT)** | `merchant2@gmail.com` | `123456` | Quản lý quán trà sữa / ăn vặt |
| **Khách hàng (USER)** | `user1@gmail.com` | `123456` | Xem món, đặt hàng, quản lý đơn hàng |
| **Khách hàng (USER)** | `user2@gmail.com` | `123456` | Xem món, đặt hàng, quản lý hồ sơ |

---

## 🎯 Các Luồng Tính Năng Nổi Bật

### 1. Khách Hàng (Customer)
- **Trang chủ & Lọc món:** Phân trang món ăn, lọc theo danh mục, tìm kiếm theo từ khóa, xem món siêu giảm giá, món ăn gần khu vực hiện tại.
- **Yêu thích (Like / Wishlist):** Thích món ăn và thích cửa hàng với cơ chế cập nhật realtime.
- **Giỏ hàng & Đặt hàng (Checkout):**
  - Đặt nhiều món từ cùng một nhà hàng.
  - Chọn địa chỉ nhận hàng và đơn vị giao hàng.
  - Tính phí ship động theo cự ly kilômét thực tế qua API HeiGIT/OpenRouteService.
  - Áp dụng mã giảm giá (Voucher code).
  - Tự động ước tính thời gian chuẩn bị món và thời gian giao hàng.
- **Lịch sử đơn hàng:** Theo dõi tiến độ đơn hàng (Chờ xác nhận -> Đang chuẩn bị -> Đang giao -> Hoàn thành / Đã hủy).

### 2. Nhà Hàng / Cửa Hàng (Merchant)
- **Đăng ký mở quán:** Gửi yêu cầu trở thành đối tác kinh doanh đến Admin.
- **Quản lý thực đơn (Menu Management):**
  - Thêm món ăn mới với nhiều hình ảnh và chọn ảnh đại diện chính.
  - Cập nhật giá bán, giá khuyến mãi, phí dịch vụ, thời gian chế biến.
  - Xóa mềm món ăn (Soft Delete) để bảo toàn dữ liệu lịch sử đơn hàng.
  - Bật/tắt trạng thái hiển thị hoặc gắn nhãn Đề xuất (Recommended).
- **Quản lý đơn hàng:**
  - Nhận đơn hàng mới từ khách hàng.
  - Cập nhật trạng thái chế biến (Xác nhận đơn, hẹn giờ chuẩn bị xong).

### 3. Quản Trị Viên (Admin)
- **Phê duyệt đối tác:** Duyệt đơn đăng ký Merchant mới hoặc khóa các cửa hàng vi phạm.
- **Đối tác vận chuyển:** Thêm mới, chỉnh sửa biểu phí cơ bản và đơn giá theo km cho các đơn vị vận chuyển (GrabFood, ShopeeFood, BeFood, Gojek, v.v.).

---

## 📚 Sơ Đồ Cơ Sở Dữ Liệu Tóm Tắt

- `users` 1 ── * `addresses` (Một người dùng có nhiều địa chỉ nhận hàng)
- `users` 1 ── 1 `merchants` (Một tài khoản đăng ký mở một cửa hàng)
- `merchants` 1 ── * `merchant_addresses` (Cửa hàng có các chi nhánh/địa chỉ)
- `merchants` 1 ── * `foods` (Cửa hàng sở hữu nhiều món ăn)
- `foods` * ── * `food_categories` (Món ăn thuộc nhiều danh mục)
- `foods` 1 ── * `food_images` (Một món ăn có nhiều hình ảnh)
- `users` 1 ── * `orders` (Khách hàng đặt nhiều đơn hàng)
- `orders` 1 ── * `order_items` (Đơn hàng chứa nhiều chi tiết món ăn)
- `delivery_partners` 1 ── * `orders` (Đơn vị vận chuyển xử lý đơn hàng)

---

## 💡 Đóng Góp & Quy Chuẩn Phát Triển

Khi thêm mới tính năng hoặc sửa lỗi, vui lòng tham khảo tài liệu [GEMINI.md](file:///Users/wanbi/Code/student-projects/MealChoice/GEMINI.md) để đảm bảo tuân thủ đúng quy chuẩn kiến trúc và ranh giới công nghệ của dự án.
