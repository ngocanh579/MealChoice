# ==============================================================================
# GEMINI.md - HƯỚNG DẪN & QUY TẮC PHÁT TRIỂN DỰ ÁN MEALCHOICE (CODEGYM)
# ==============================================================================

> **Mục đích tài liệu:** Thiết lập tiêu chuẩn phát triển, ranh giới công nghệ và phong cách lập trình cho AI Assistants / Developers khi làm việc trên dự án **MealChoice**. Đảm bảo code sinh ra chuẩn mực, sạch sẽ, bám sát khung chương trình học của học viên (Java & Spring Framework).

---

## 1. BỐI CẢNH DỰ ÁN & RANH GIỚI CÔNG NGHỆ (TECH STACK BOUNDARY)

Dự án **MealChoice** là ứng dụng web đặt đồ ăn trực tuyến (Online Food Ordering & Delivery Platform) được xây dựng phục vụ học tập và đánh giá năng lực học viên tại CodeGym.

### 📌 Công nghệ bắt buộc (Strict Tech Stack):
- **Ngôn ngữ:** Java 17 LTS (sử dụng cú pháp chuẩn Java 17: Record, Text blocks, Pattern matching instanceof, Stream API, etc.).
- **Build Tool:** Gradle (Groovy DSL - `build.gradle`).
- **Framework nền tảng:** Spring Boot 3.x / 4.x
  - **Spring MVC**: Xử lý request, điều hướng View SSR.
  - **Spring Data JPA & Hibernate**: Tương tác cơ sở dữ liệu ORM.
  - **Spring Security 6 & JJWT**: Xác thực người dùng, phân quyền Role-based, JWT Filter kết hợp Cookie/Header.
  - **Spring Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, v.v.).
  - **Spring Mail**: Gửi email kích hoạt tài khoản / thông báo.
- **Template Engine:** Thymeleaf + Thymeleaf Extras Spring Security.
- **Cơ sở dữ liệu:** MySQL 8.0+ / InnoDB / Charset `utf8mb4`.
- **Frontend:** HTML5, CSS3 (Custom CSS), Bootstrap 5, Vanilla JavaScript (ES6+ Fetch API, DOM manipulation).
- **Thư viện bổ trợ:** Project Lombok (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).

---

## 2. NHỮNG ĐIỀU TUYỆT ĐỐI KHÔNG LÀM (PROHIBITED & ANTI-PATTERNS)

Để tránh **lệch kiến thức** hoặc làm dự án trở nên quá tải đối với học viên:

1. ❌ **KHÔNG** tự ý đưa các framework Frontend phức tạp (React, Angular, Vue, Next.js) vào dự án nếu không có yêu cầu cụ thể. Luôn sử dụng **Thymeleaf + Vanilla JS/Bootstrap**.
2. ❌ **KHÔNG** sử dụng kiến trúc Microservices, Event-Driven Broker phức tạp (Kafka, RabbitMQ), Reactive Programming (Spring WebFlux) hay NoSQL/Redis trừ khi được hướng dẫn.
3. ❌ **KHÔNG** viết code quá trừu tượng (Over-engineering) như Dynamic Proxies, Custom Bytecode, Reflection phức tạp.
4. ❌ **KHÔNG** hardcode thông tin nhạy cảm (mật khẩu database, email app password, jwt secret, api key) trực tiếp trong file mã nguồn.
5. ❌ **KHÔNG** query lặp trong vòng for gây ra lỗi **N+1 Query** trong JPA/Hibernate.
6. ❌ **KHÔNG** trả Entity trực tiếp ra ngoài REST API nếu Entity đó có dữ liệu nhạy cảm (`password`, `activationTokens`) hoặc quan hệ 2 chiều gây vòng lặp JSON (Infinite Recursion).

---

## 3. QUY CHUẨN THIẾT KẾ MÃ NGUỒN (CLEAN ARCHITECTURE)

Dự án áp dụng mô hình phân lớp chuẩn (Layered Architecture):

```
Controller / RestController (Giao tiếp HTTP, nhận request, trả View hoặc DTO)
         ↓
Service Interface & Implementation (Xử lý nghiệp vụ, Transactional logic)
         ↓
Repository Layer (Spring Data JPA, JPQL, EntityGraph)
         ↓
Database Layer (MySQL Entities)
```

### 3.1. Entity Layer (`vn.codegyme.meal_choice.entity`)
- Định nghĩa đúng quan hệ: `@ManyToOne(fetch = FetchType.LAZY)`, `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL, orphanRemoval = true)`, `@ManyToMany`.
- Sử dụng UUID hoặc Long ID nhất quán theo từng bảng:
  - `users`, `merchants`, `delivery_partners`, `activation_tokens`, `refresh_tokens`: UUID (chuỗi 36 ký tự).
  - `foods`, `food_categories`, `tags`, `orders`, `order_items`, `roles`: Long (AUTO_INCREMENT).
- Bổ sung `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` trên các Entity có ID để tránh vòng lặp hashCode trong Hibernate collection.
- Áp dụng `@CreationTimestamp`, `@UpdateTimestamp` hoặc `@PrePersist`, `@PreUpdate` để tự động quản lý thời gian.

### 3.2. Repository Layer (`vn.codegyme.meal_choice.repository`)
- Kế thừa `JpaRepository<Entity, ID>`.
- Ưu tiên sử dụng Derived Query Methods của Spring Data JPA.
- Với các truy vấn phức tạp hoặc cần JOIN nạp dữ liệu:
  - Dùng `@EntityGraph(attributePaths = {"orderItems", "merchant", "user"})` để tránh N+1.
  - Dùng JPQL `@Query("SELECT DISTINCT f FROM Food f JOIN FETCH f.foodCategories c WHERE ...")`.
  - Luôn hỗ trợ `Pageable` và trả về `Page<T>` hoặc `Slice<T>` khi truy vấn danh sách lớn.

### 3.3. Service Layer (`vn.codegyme.meal_choice.service`)
- Tuân thủ nguyên tắc: **Giao diện (Interface)** đặt trong `vn.codegyme.meal_choice.service`, **Lớp triển khai (Impl)** đặt trong `vn.codegyme.meal_choice.service.impl`.
- Đánh dấu `@Transactional` cho các phương thức có ghi/cập nhật dữ liệu (CREATE, UPDATE, DELETE).
- Đánh dấu `@Transactional(readOnly = true)` cho các phương thức chỉ đọc (GET / SELECT) để tối ưu hiệu năng Hibernate dirty-checking.
- Ném ra các Exception tường minh (`IllegalArgumentException`, `ResourceNotFoundException`, hoặc Exception nghiệp vụ rõ ràng).

### 3.4. DTO & Validation Layer (`vn.codegyme.meal_choice.dto`)
- Tạo DTO riêng biệt cho Request (`CreateRequest`, `UpdateRequest`, `LoginRequest`) và Response (`ResponseDTO`).
- Sử dụng các annotation của Jakarta Validation:
  - `@NotBlank(message = "Tên không được để trống")`
  - `@NotNull(message = "Giá tiền không được để trống")`
  - `@Min(value = 0, message = "Giá trị tối thiểu là 0")`
  - `@Email(message = "Email không đúng định dạng")`
  - `@Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")`

### 3.5. Controller Layer (`vn.codegyme.meal_choice.controller`)
- **SSR Page Controller (`@Controller`)**:
  - Dành cho trả về giao diện Thymeleaf (ví dụ `/`, `/login`, `/food/{id}`, `/merchant/dashboard`).
  - Gán dữ liệu vào `org.springframework.ui.Model`.
- **REST API Controller (`@RestController`)**:
  - Định tuyến URL bắt đầu bằng `/api/...` (ví dụ `/api/auth/login`, `/api/foods`, `/api/orders`).
  - Sử dụng `@Valid @RequestBody` để validate DTO.
  - Trả về `ResponseEntity<T>` với đúng HTTP Status code (`200 OK`, `201 CREATED`, `204 NO_CONTENT`, `400 BAD_REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN`, `404 NOT_FOUND`).

### 3.6. Xử lý lỗi tập trung (`GlobalExceptionHandler`)
- Định dạng phản hồi lỗi API nhất quán bằng `ErrorResponse` (chứa `status`, `message`, `timestamp`).
- Bắt và chuyển đổi các lỗi `MethodArgumentNotValidException` thành thông báo lỗi dễ hiểu cho người dùng/học viên.

---

## 4. QUY ƯỚC PHÂN QUYỀN & BẢO MẬT (SECURITY CONVENTIONS)

1. **Role Convention:** Hệ thống có 3 vai trò chính:
   - `ROLE_USER` (Khách hàng): Đăng nhập, quản lý địa chỉ, xem menu, đặt hàng, theo dõi đơn hàng, thích món ăn/quán ăn, đăng ký mở quán.
   - `ROLE_MERCHANT` (Chủ nhà hàng): Sau khi được Admin duyệt, có quyền quản lý cửa hàng, món ăn, menu, cập nhật trạng thái đơn hàng.
   - `ROLE_ADMIN` (Quản trị viên): Quản lý người dùng, duyệt/khóa Merchant, quản lý đơn vị vận chuyển (Delivery Partner).
2. **Security Config:**
   - Stateless session với JWT cho API.
   - Cookie `accessToken` cho phép Thymeleaf page gọi AJAX API một cách mượt mà.
   - Luôn cấu hình `AuthenticationEntryPoint` để phân biệt:
     - Request bắt đầu bằng `/api/...` trả về `401 Unauthorized (JSON)`.
     - Request trình duyệt (HTML) chuyển hướng sang `/login`.

---

## 5. HƯỚNG DẪN VIẾT CODE & GIẢI THÍCH CHO HỌC VIÊN

- **Chú thích (Comments):** Viết chú thích bằng tiếng Việt rõ ràng ở các bước logic nghiệp vụ chính (BƯỚC 1, BƯỚC 2, BƯỚC 3).
- **Tính sư phạm:** Giải thích rõ *tại sao lại làm như vậy* (ví dụ: tại sao cần `@Transactional`, tại sao cần `@EntityGraph`, tại sao cần DTO).
- **Tính nhất quán:** Giữ phong cách đặt tên biến tiếng Anh chuẩn ngữ nghĩa (`orderRepository`, `foodService`, `userEmail`, `totalAmount`), tên method rõ ràng (`findApprovedMerchantById`, `calculateShippingFee`).
