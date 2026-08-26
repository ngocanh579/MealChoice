# ==============================================================================
# GEMINI.md - HƯỚNG DẪN & QUY TẮC PHÁT TRIỂN DỰ ÁN MEALCHOICE (CODEGYM)
# ==============================================================================

> **Bối cảnh dự án:** Dự án **"Nhóm A - Trưa nay ăn gì" (MealChoice)** là đồ án web đặt & giao đồ ăn trực tuyến (Food Ordering & Delivery Platform) dành cho học viên CodeGym thực hành chuyên sâu môn Java & Spring Framework.

---

## 1. RANH GIỚI CÔNG NGHỆ BẮT BUỘC (STRICT TECH STACK)

Để bám sát khung chương trình học và tránh làm quá tải hoặc lệch kiến thức học viên:

- **Ngôn ngữ:** Java 17 LTS (sử dụng cú pháp chuẩn: Record, Text blocks, Pattern matching instanceof, Stream API).
- **Build Tool:** Gradle (Groovy DSL - `build.gradle`).
- **Framework nền tảng:** Spring Boot 3.x / 4.x
  - **Spring MVC**: Xử lý HTTP Request, điều hướng View SSR.
  - **Spring Data JPA & Hibernate**: Tương tác cơ sở dữ liệu ORM.
  - **Spring Security 6 & JJWT**: Xác thực người dùng, phân quyền Role-based, JWT Filter kết hợp Cookie/Header.
  - **Spring Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Min`, v.v.).
  - **Spring Mail**: Gửi email kích hoạt tài khoản / thông báo trạng thái.
- **Template Engine:** Thymeleaf + Thymeleaf Extras Spring Security.
- **Cơ sở dữ liệu:** MySQL 8.0+ / InnoDB / Charset `utf8mb4`.
- **Frontend:** HTML5, CSS3, Bootstrap 5, Vanilla JavaScript (ES6+ Fetch API, DOM manipulation).
- **Thư viện bổ trợ:** Project Lombok (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).

---

## 2. NHỮNG ĐIỀU TUYỆT ĐỐI KHÔNG LÀM (PROHIBITED & ANTI-PATTERNS)

1. ❌ **KHÔNG** tự ý đưa các framework Frontend phức tạp (React, Angular, Vue, Next.js) vào dự án nếu không có yêu cầu cụ thể. Luôn sử dụng **Thymeleaf + Vanilla JS/Bootstrap**.
2. ❌ **KHÔNG** sử dụng kiến trúc Microservices, Event-Driven Broker phức tạp (Kafka, RabbitMQ), Reactive Programming (Spring WebFlux) hay NoSQL/Redis.
3. ❌ **KHÔNG** viết code quá trừu tượng (Over-engineering) như Dynamic Proxies, Custom Bytecode, Reflection phức tạp.
4. ❌ **KHÔNG** hardcode thông tin nhạy cảm (mật khẩu database, email app password, jwt secret, api key) trực tiếp trong file mã nguồn.
5. ❌ **KHÔNG** query lặp trong vòng for gây ra lỗi **N+1 Query** trong JPA/Hibernate.
6. ❌ **KHÔNG** trả Entity trực tiếp ra ngoài REST API nếu Entity đó có dữ liệu nhạy cảm (`password`, `activationTokens`) hoặc quan hệ 2 chiều gây vòng lặp JSON (Infinite Recursion).

---

## 3. QUY TẮC NGHIỆP VỤ ĐẶC THÙ (BUSINESS RULES TỪ SPEC TRELLO)

Tất cả các tính năng phát triển phải tuân thủ đúng các quy tắc nghiệp vụ trong tài liệu đặc tả của nhóm:

### 3.1. Quản lý Đơn hàng & Trạng thái (Order Lifecycle)
- **Luồng trạng thái:** `PENDING` (Chờ nhận hàng) $\rightarrow$ `PREPARING` (Đang chuẩn bị) $\rightarrow$ `DELIVERING` (Đang giao) $\rightarrow$ `COMPLETED` (Hoàn thành) / `CANCELLED` (Đã hủy).
- **Ràng buộc hủy đơn:**
  - **Khách hàng (User):** Chỉ được hủy đơn khi cửa hàng chưa giao (trạng thái khác `DELIVERING` và `COMPLETED`).
  - **Cửa hàng (Merchant):** Chỉ được hủy đơn khi đang ở trạng thái `PENDING` ("Chờ nhận hàng").
- **Hẹn giờ chuẩn bị:** Khi Merchant nhận đơn chuyển sang `PREPARING`, cho phép nhập thời gian chuẩn bị (phút) để tính toán `preparingUntil` và tự động chuyển trạng thái.

### 3.2. Đối soát Doanh thu Merchant (Settlement & Payout)
- **Công thức tính dòng tiền:**
  $$\text{Doanh thu thực nhận} = \text{Giá sản phẩm} - \text{Khuyến mãi} - \text{Chiết khấu sàn (Phí sàn)}$$
- **Biểu phí chiết khấu sàn:**
  - $0.001\% / \text{đơn}$ (Nếu tổng doanh thu tháng $< 200 \text{ triệu VNĐ}$).
  - $0.0005\% / \text{đơn}$ (Nếu tổng doanh thu tháng $\ge 200 \text{ triệu VNĐ}$).
- **Trạng thái kỳ đối soát:** `Chờ xác nhận` $\rightarrow$ `Đã xác nhận` (Chuyển Admin làm lệnh Payout) hoặc `Đang khiếu nại` (Tạm khóa chuyển tiền, gửi ticket sang Admin).
- **Đối tác thân thiết / Thanh lý hợp đồng:** Merchant phải thỏa mãn điều kiện **Doanh thu tháng $> 100 \text{ triệu VNĐ}$** mới được đăng ký Đối tác thân thiết hoặc yêu cầu thanh lý rút hết tiền.

### 3.3. Món ăn & Tìm kiếm (Food & Catalog)
- **Thêm món mới:** Bắt buộc có ít nhất 2 hình ảnh, gắn địa chỉ chi nhánh, chọn Tag, cài đặt thời gian chuẩn bị (phút) và phí dịch vụ.
- **Quick search trên trang chủ:** Hỗ trợ lọc nhanh theo 4 nhóm bữa ăn: `Breakfast`, `Lunch`, `Dinner`, `Café`.
- **Gợi ý trang chủ:** Hiển thị 8 món ăn giảm giá nhiều nhất và 8 món ăn gần vị trí người dùng nhất.

---

## 4. QUY CHUẨN THIẾT KẾ MÃ NGUỒN (CLEAN ARCHITECTURE)

```
Controller / RestController (Giao tiếp HTTP, nhận request, trả View hoặc DTO)
         ↓
Service Interface & Implementation (Xử lý nghiệp vụ, Transactional logic)
         ↓
Repository Layer (Spring Data JPA, JPQL, EntityGraph)
         ↓
Database Layer (MySQL Entities)
```

### 4.1. Entity Layer (`vn.codegyme.meal_choice.entity`)
- Quan hệ `@ManyToOne(fetch = FetchType.LAZY)`, `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL, orphanRemoval = true)`.
- Sử dụng UUID (chuỗi 36 ký tự) cho `User`, `Merchant`, `DeliveryPartner`, `ActivationToken`, `RefreshToken`.
- Sử dụng Long (AUTO_INCREMENT) cho `Food`, `FoodCategory`, `Tag`, `Order`, `OrderItem`, `Role`, `Voucher`.
- Bổ sung `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` trên các Entity có ID.

### 4.2. Repository Layer (`vn.codegyme.meal_choice.repository`)
- Kế thừa `JpaRepository<Entity, ID>`.
- Tránh N+1 bằng `@EntityGraph(attributePaths = {"orderItems", "merchant", "user"})` hoặc JPQL `JOIN FETCH`.
- Luôn hỗ trợ `Pageable` khi truy vấn danh sách lớn.

### 4.3. Service Layer (`vn.codegyme.meal_choice.service`)
- Tuân thủ nguyên tắc: **Giao diện (Interface)** đặt trong `service`, **Lớp triển khai (Impl)** đặt trong `service.impl`.
- Đánh dấu `@Transactional` cho các thao tác ghi/sửa và `@Transactional(readOnly = true)` cho các thao tác đọc.
- Tận dụng cơ chế **Dirty Checking** của Hibernate để cập nhật Entity, tránh gọi `save()` lặp trong vòng for.

### 4.4. DTO & Controller Layer (`vn.codegyme.meal_choice.dto` & `controller`)
- DTO tách biệt Request/Response và có validation Jakarta đầy đủ (`@NotBlank`, `@NotNull`, `@Min`, `@Email`).
- Tách bạch `@Controller` (trả về View Thymeleaf) và `@RestController` (tiền tố `/api/...` trả về JSON `ResponseEntity<T>`).

---

## 5. HƯỚNG DẪN VIẾT CODE & GIẢI THÍCH CHO HỌC VIÊN

- **Chú thích (Comments):** Viết chú thích bằng tiếng Việt rõ ràng ở các bước logic nghiệp vụ chính (BƯỚC 1, BƯỚC 2, BƯỚC 3).
- **Tính sư phạm:** Giải thích rõ *tại sao lại làm như vậy* (ví dụ: tại sao cần `@Transactional`, tại sao cần `@EntityGraph`, tại sao cần DTO).
- **Tính nhất quán:** Giữ phong cách đặt tên biến tiếng Anh chuẩn ngữ nghĩa (`orderRepository`, `foodService`, `userEmail`, `totalAmount`), tên method rõ ràng (`findApprovedMerchantById`, `calculateShippingFee`).
