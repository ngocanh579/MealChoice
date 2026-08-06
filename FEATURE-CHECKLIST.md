# Đối chiếu nhiệm vụ

| ID | User story | Phần triển khai |
|---:|---|---|
| 1 | Merchant đăng ký thành viên | `/merchant/register`, `POST /api/merchants/register` |
| 2 | Merchant cập nhật cửa hàng | `/merchant/profile`, `GET/PUT /api/merchant/profile` |
| 25 | Admin duyệt/từ chối merchant | `PATCH /api/admin/merchants/{id}/decision` |
| 26 | Admin xem danh sách merchant | `/admin/merchants`, `GET /api/admin/merchants` |
| 27 | Admin xem chi tiết merchant | `/admin/merchants/{id}`, `GET /api/admin/merchants/{id}` |
| 28 | Admin khóa/mở khóa merchant | `PATCH /api/admin/merchants/{id}/lock` |
| 29 | Admin duyệt đối tác thân thiết | `PATCH /api/admin/merchants/{id}/loyal-partner` |
| 37 | Menu trên cùng có tài khoản và giỏ hàng | `layout/navbar.html`, `static/js/top-menu.js` |
| 42 | Footer hiển thị thông tin chính xác | `layout/footer.html`, năm bản quyền cập nhật tự động |
| 53 | Người dùng đăng ký tài khoản | `/register`, `POST /api/auth/register` |
| 54 | Người dùng đăng nhập | `/login`, `POST /api/auth/login` |
| 55 | Gửi email kích hoạt sau đăng ký | `AccountActivationEmailService`, `VerificationToken` |
| 56 | Kích hoạt khi nhấn liên kết email | `GET /api/account/verify`, `/verify-success` |
| 64 | Cập nhật thông tin cá nhân | `/user/profile`, `PATCH /api/user/profile` |
| 65 | Thêm địa chỉ giao hàng | `POST /api/user/address` |
| 66 | Sửa địa chỉ giao hàng | `PATCH /api/user/address/{id}` |
| 67 | Xóa địa chỉ giao hàng | `DELETE /api/user/address/{id}` |

## Nguyên tắc tích hợp

- Không xóa hoặc thay nội dung dòng code gốc trong ZIP.
- Tái sử dụng chức năng hồ sơ/địa chỉ đã có; chỉ bổ sung phần còn thiếu.
- Trạng thái đối tác thân thiết và lịch sử quản trị nằm trong bảng phụ `merchant_admin_profiles`, tránh thay đổi entity Merchant cũ.
- User mới không nhận JWT trước khi kích hoạt email. Merchant bị khóa/từ chối không thể đăng nhập hoặc làm mới refresh token.
