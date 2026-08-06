# MealChoice – Trưa nay ăn gì?

Dự án Spring Boot MVC + Thymeleaf + JPA cho quản lý tài khoản người dùng và merchant.

## Yêu cầu chạy

- Java 17
- MySQL 8+
- Gradle Wrapper đi kèm dự án

## Cấu hình

1. Tạo MySQL hoặc để ứng dụng tự tạo database `meal_choice`.
2. Kiểm tra `src/main/resources/application.properties` và thay thông tin MySQL/SMTP bằng cấu hình của máy chạy.
3. Nếu chạy cổng khác, thêm `server.port=8081` và `app.base-url=http://localhost:8081` để liên kết kích hoạt email dùng đúng cổng.
4. Chạy `gradlew bootRun` trên Windows hoặc `./gradlew bootRun` trên macOS/Linux.

Ở môi trường phát triển, tài khoản quản trị mặc định là `admin@mealchoice.com` / `Admin@123`. Hãy đổi bằng các thuộc tính `app.admin.email`, `app.admin.password`, `app.admin.phone` trước khi triển khai thật.

## Các luồng đã hoàn thiện

- Người dùng: đăng ký, nhận email kích hoạt, kích hoạt bằng liên kết, đăng nhập, cập nhật hồ sơ.
- Địa chỉ: xem, thêm, sửa, xóa và đặt địa chỉ mặc định.
- Merchant: đăng ký thành viên và cập nhật thông tin cửa hàng.
- Admin: danh sách/chi tiết merchant, duyệt/từ chối, khóa/mở khóa và duyệt đối tác thân thiết.
- Giao diện dùng chung: menu tài khoản, số lượng giỏ hàng và footer cập nhật năm tự động.

## Đường dẫn chính

- `/login`, `/register`, `/verify-success`
- `/user/profile`, `/user/address`
- `/merchant/register`, `/merchant/profile`
- `/admin/merchants`, `/admin/merchants/{id}`

Các API hồ sơ và quản trị yêu cầu JWT trong header `Authorization: Bearer <token>`. API Admin đồng thời yêu cầu role `ROLE_ADMIN`.
