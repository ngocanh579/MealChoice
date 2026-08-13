-- ====================================================================
-- DATABASE SCHEMA & SAMPLE DATA CHO DỰ ÁN MEAL CHOICE
-- ====================================================================

CREATE DATABASE IF NOT EXISTS meal_choice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE meal_choice;

-- Dọn dẹp các bảng cũ nếu đã tồn tại
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS merchant_likes;
DROP TABLE IF EXISTS food_likes;
DROP TABLE IF EXISTS food_images;
DROP TABLE IF EXISTS foods;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS merchant_addresses;
DROP TABLE IF EXISTS merchants;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================================
-- 1. TẠO CÁC BẢNG (DDL SCHEMA)
-- ====================================================================

-- Roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Users
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(60) NOT NULL,
    display_name VARCHAR(32) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    avatar_url VARCHAR(255),
    dob DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- User Roles
CREATE TABLE user_roles (
    user_id VARCHAR(36),
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Addresses (Địa chỉ khách hàng)
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    street VARCHAR(255) NOT NULL,
    note VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    user_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Merchants (Quán ăn / Nhà hàng)
CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE,
    merchant_restaurant_name VARCHAR(150) NOT NULL,
    merchant_email VARCHAR(255) UNIQUE NOT NULL,
    merchant_phone VARCHAR(20) UNIQUE NOT NULL,
    merchant_status VARCHAR(30) DEFAULT 'PENDING',
    lock_reason VARCHAR(500),
    locked_at DATETIME(6),
    reject_reason VARCHAR(500),
    rejected_at DATETIME(6),
    is_trusted_partner BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Merchant Addresses (Địa chỉ và chi nhánh của Merchant)
CREATE TABLE merchant_addresses (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_address VARCHAR(255) NOT NULL,
    province_code VARCHAR(20),
    district_code VARCHAR(20),
    ward_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    merchant_open_time TIME,
    merchant_close_time TIME,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (merchant_id) REFERENCES merchants (id) ON DELETE CASCADE
);

-- Categories (Danh mục món ăn)
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    category_description VARCHAR(255),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Foods (Món ăn)
CREATE TABLE foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    preparation_time INT,
    note VARCHAR(500),
    price DOUBLE NOT NULL,
    discount_price DOUBLE,
    service_fee DOUBLE DEFAULT 0,
    tag VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    views INT DEFAULT 0,
    order_count INT DEFAULT 0,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6),
    merchant_id VARCHAR(36),
    merchant_address_id VARCHAR(36),
    category_id BIGINT NOT NULL,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id) ON DELETE CASCADE,
    FOREIGN KEY (merchant_address_id) REFERENCES merchant_addresses (id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

-- Food Images (Nhiều ảnh cho 1 món ăn)
CREATE TABLE food_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    food_id BIGINT NOT NULL,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE
);

-- Food Likes (Lượt thích món ăn)
CREATE TABLE food_likes (
    user_id VARCHAR(36) NOT NULL,
    food_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, food_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE
);

-- Merchant Likes (Lượt thích quán ăn)
CREATE TABLE merchant_likes (
    user_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, merchant_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id) ON DELETE CASCADE
);

-- ====================================================================
-- 2. DỮ LIỆU MẪU ĐỂ TEST HỆ THỐNG
-- ====================================================================

-- Thêm Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER'),
(3, 'ROLE_MERCHANT');

-- Thêm Users mẫu (Mật khẩu '123456' hash BCrypt)
INSERT INTO users (id, email, password, display_name, phone_number, gender, avatar_url, dob, is_active, created_at) VALUES 
('00000000-0000-0000-0000-000000000001', 'admin@mealchoice.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Admin', '0900000001', 'MALE', NULL, '1990-01-01', TRUE, NOW()),
('00000000-0000-0000-0000-000000000002', 'an@gmail.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Nguyen Van An', '0900000002', 'MALE', NULL, '2000-05-10', TRUE, NOW()),
('00000000-0000-0000-0000-000000000003', 'binh@gmail.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Tran Thi Binh', '0900000003', 'FEMALE', NULL, '2001-08-15', TRUE, NOW()),
('00000000-0000-0000-0000-000000000004', 'nam@merchant.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Le Van Nam', '0900000004', 'MALE', NULL, '1995-03-20', TRUE, NOW()),
('00000000-0000-0000-0000-000000000005', 'hoa@merchant.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Pham Thi Hoa', '0900000005', 'FEMALE', NULL, '1996-11-25', TRUE, NOW()),
('00000000-0000-0000-0000-000000000006', 'minh@merchant.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Nguyen Van Minh', '0900000006', 'MALE', NULL, '1994-02-02', TRUE, NOW()),
('00000000-0000-0000-0000-000000000007', 'lan@merchant.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Do Thi Lan', '0900000007', 'FEMALE', NULL, '1997-06-06', TRUE, NOW()),
('00000000-0000-0000-0000-000000000008', 'duc@merchant.com', '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO', 'Hoang Duc', '0900000008', 'MALE', NULL, '1993-09-09', TRUE, NOW());

-- Phân quyền User Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
('00000000-0000-0000-0000-000000000001', 1),
('00000000-0000-0000-0000-000000000002', 2),
('00000000-0000-0000-0000-000000000003', 2),
('00000000-0000-0000-0000-000000000004', 2),
('00000000-0000-0000-0000-000000000004', 3),
('00000000-0000-0000-0000-000000000005', 2),
('00000000-0000-0000-0000-000000000005', 3),
('00000000-0000-0000-0000-000000000006', 2),
('00000000-0000-0000-0000-000000000006', 3),
('00000000-0000-0000-0000-000000000007', 2),
('00000000-0000-0000-0000-000000000007', 3),
('00000000-0000-0000-0000-000000000008', 2),
('00000000-0000-0000-0000-000000000008', 3);

-- Địa chỉ người dùng (để test gợi ý món ăn theo vị trí Quận 1, Quận 3)
INSERT INTO addresses (id, contact_name, contact_phone, city, district, ward, street, is_default, user_id) VALUES 
(1, 'Nguyen Van An', '0900000002', 'Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '100 Nguyễn Huệ', TRUE, '00000000-0000-0000-0000-000000000002'),
(2, 'Tran Thi Binh', '0900000003', 'Hồ Chí Minh', 'Quận 3', 'Phường Võ Thị Sáu', '250 Pasteur', TRUE, '00000000-0000-0000-0000-000000000003');

-- Thêm Merchants (Quán ăn / Nhà hàng)
INSERT INTO merchants (id, user_id, merchant_restaurant_name, merchant_email, merchant_phone, merchant_status, is_trusted_partner) VALUES 
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', 'Cơm Tấm Cali - Hương Vị Sài Gòn', 'nam@merchant.com', '0900000004', 'APPROVED', TRUE),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', 'Bếp Nhà Hoa - Món Ăn Gia Đình', 'hoa@merchant.com', '0900000005', 'APPROVED', TRUE),
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000006', 'The Pizza Box & Italian Pasta', 'minh@merchant.com', '0900000006', 'APPROVED', TRUE),
('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000007', 'Tiệm Trà Sữa Mộc & Bakery', 'lan@merchant.com', '0900000007', 'APPROVED', TRUE),
('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000008', 'Bún Chả & Phở Hà Thành 1979', 'duc@merchant.com', '0900000008', 'APPROVED', TRUE);

-- Thêm Merchant Addresses
INSERT INTO merchant_addresses (id, merchant_id, merchant_address, province_code, district_code, ward_code, is_default, merchant_open_time, merchant_close_time) VALUES 
('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '123 Nguyễn Thị Minh Khai, Phường Bến Thành, Quận 1, Hồ Chí Minh', '79', '760', '26740', TRUE, '07:00:00', '22:00:00'),
('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '50 Nguyễn Đình Chiểu, Phường Đa Kao, Quận 1, Hồ Chí Minh', '79', '760', '26734', TRUE, '08:00:00', '21:30:00'),
('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', '220 Pasteur, Phường Võ Thị Sáu, Quận 3, Hồ Chí Minh', '79', '770', '27145', TRUE, '09:00:00', '22:30:00'),
('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', '15 Tôn Thất Tùng, Phường Phạm Ngũ Lão, Quận 1, Hồ Chí Minh', '79', '760', '26743', TRUE, '08:30:00', '22:30:00'),
('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', '45 Lê Duẩn, Phường Bến Nghé, Quận 1, Hồ Chí Minh', '79', '760', '26737', TRUE, '06:30:00', '21:30:00');

-- Thêm 10 Danh mục Món ăn (Categories)
INSERT INTO categories (id, name, category_description) VALUES 
(1, 'Cơm trưa', 'Các món cơm văn phòng, cơm tấm, cơm chiên nóng hổi'),
(2, 'Phở & Bún', 'Phở bò, bún bò, bún chả thơm lừng truyền thống'),
(3, 'Pizza & Pasta', 'Pizza phô mai kéo sợi và mì Ý đậm sốt kiểu Âu'),
(4, 'Gà Rán & Burger', 'Gà rán giòn rụm và burger bò phô mai đẫm sốt'),
(5, 'Món Nhật & Sushi', 'Sashimi tươi ngon, ramen và sushi thượng hạng'),
(6, 'Món Hàn Quốc', 'Tokbokki, gà sốt cay phô mai và cơm trộn thố đá'),
(7, 'Trà Sữa & Đồ Uống', 'Trà sữa trân châu hoàng kim, trà trái cây thanh nhiệt'),
(8, 'Bánh Mì & Ăn Vặt', 'Bánh mì giòn rụm và các món ăn vặt hấp dẫn'),
(9, 'Healthy & Salad', 'Salad ức gà, thực đơn Eat Clean dinh dưỡng ít calo'),
(10, 'Lẩu & Nướng', 'Lẩu Thái tomyum hải sản và đồ nướng than hoa');

-- Thêm 32 Món ăn (Foods) phong phú với ảnh Unsplash đẹp, giá gốc và giá khuyến mãi
INSERT INTO foods (id, name, address, image_url, preparation_time, note, price, discount_price, service_fee, tag, is_active, created_at, merchant_id, merchant_address_id, category_id) VALUES 
(1, 'Cơm Tấm Sườn Bì Chả Trứng Ốp La', '123 Nguyễn Thị Minh Khai, Quận 1', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80', 15, 'Sườn nướng mật ong thơm lừng kèm nước mắm kẹo', 65000, 45000, 3000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 1),
(2, 'Cơm Sườn Cây Khổng Lồ Nướng Muối Ớt', '123 Nguyễn Thị Minh Khai, Quận 1', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80', 20, 'Sườn cây đẫm sốt cay đậm đà chuẩn vị', 85000, 55000, 3000, 'Nổi Bật', TRUE, NOW(), '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 1),
(3, 'Cơm Chiên Hải Sản Hoàng Kim', '123 Nguyễn Thị Minh Khai, Quận 1', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=600&auto=format&fit=crop&q=80', 15, 'Cơm chiên hạt tơi vàng óng kèm tôm mực tươi', 70000, 50000, 3000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 1),
(4, 'Cơm Bò Xào Hành Cần Sốt Tiêu Đen', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop&q=80', 15, 'Thịt bò mềm ngấm sốt tiêu cay nhẹ', 75000, 60000, 3000, 'Đề Xuất', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 1),
(5, 'Phở Bò Tái Nạm Gầu Bắp Bò', '45 Lê Duẩn, Bến Nghé, Quận 1', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=600&auto=format&fit=crop&q=80', 15, 'Nước dùng hầm xương 24h thanh ngọt gia truyền', 60000, 39000, 2000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 2),
(6, 'Phở Gà Ta Đồi Thịt Chắc Thơm', '45 Lê Duẩn, Bến Nghé, Quận 1', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', 15, 'Thịt gà ta da giòn thịt ngọt kèm lá chanh', 55000, 42000, 2000, 'Yêu Thích', TRUE, NOW(), '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 2),
(7, 'Bún Chả Hà Nội Nướng Than Hoa', '45 Lê Duẩn, Bến Nghé, Quận 1', 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=600&auto=format&fit=crop&q=80', 15, 'Chả viên chả miếng nướng vàng rộm thơm lừng', 65000, 48000, 2000, 'Đề Xuất', TRUE, NOW(), '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 2),
(8, 'Bún Bò Huế Đặc Biệt Giò Chả', '45 Lê Duẩn, Bến Nghé, Quận 1', 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', 20, 'Nước dùng thơm mùi sả ruốc đậm đà miền Trung', 70000, 52000, 2000, 'Nổi Bật', TRUE, NOW(), '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 2),
(9, 'Pizza Hải Sản Pesto Xanh Tươi Độc Quyền', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80', 25, 'Tôm mực tươi rói phủ phô mai Mozzarella béo ngậy', 189000, 99000, 5000, 'Giảm 50%', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 3),
(10, 'Pizza Bò Băm Phô Mai Double Cheese', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&auto=format&fit=crop&q=80', 20, 'Đế bánh giòn rụm nhân thịt bò thượng hạng', 165000, 99000, 5000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 3),
(11, 'Mì Ý Sốt Bò Băm Bolognese Cổ Điển', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1621996346565-e3d5d6281691?w=600&auto=format&fit=crop&q=80', 15, 'Sợi mì al dente sốt cà chua thịt bò đậm đà', 89000, 59000, 3000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 3),
(12, 'Mì Ý Sốt Kem Nấm Carbonara Thịt Xông Khói', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=600&auto=format&fit=crop&q=80', 15, 'Sốt kem béo ngậy cùng thịt xông khói áp chảo', 95000, 65000, 3000, 'Đề Xuất', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 3),
(13, 'Combo Gà Rán Giòn Cay 4 Miếng Kèm Khoai', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=600&auto=format&fit=crop&q=80', 20, 'Lớp bột giòn tan, thịt gà mềm mọng nước', 150000, 75000, 3000, 'Giảm 50%', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 4),
(14, 'Burger Bò Phô Mai Nướng 2 Tầng Double Beef', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80', 15, '2 miếng bò nướng than hoa kẹp phô mai Cheddar', 85000, 49000, 3000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 4),
(15, 'Gà Popcorn Lắc Phô Mai Cay Nồng', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1562967914-608f82629710?w=600&auto=format&fit=crop&q=80', 10, 'Gà viên giòn rụm lắc ngập bột phô mai thơm nức', 45000, 29000, 2000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 4),
(16, 'Burger Tôm Giòn Sốt Tartar Béo Ngậy', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&auto=format&fit=crop&q=80', 15, 'Miếng tôm xay nguyên con giòn xốp', 75000, 48000, 3000, 'Yêu Thích', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 4),
(17, 'Set Sushi Sashimi Cá Hồi Thượng Hạng', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80', 25, 'Cá hồi tươi nhập khẩu Nauy tươi ngon béo ngậy', 320000, 160000, 5000, 'Giảm 50%', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 5),
(18, 'Cơm Lươn Nhật Nướng Sốt Teriyaki Unagi', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=600&auto=format&fit=crop&q=80', 20, 'Lươn nướng sốt ngọt đậm đà chuẩn vị Kyoto', 195000, 125000, 5000, 'Nổi Bật', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 5),
(19, 'Mì Ramen Xương Hầm Tonkotsu Thịt Chashu', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', 15, 'Nước hầm xương heo sánh ngậy cùng trứng lòng đào', 110000, 75000, 3000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 5),
(20, 'Cơm Bò Wagyu Trứng Onsen Nhật Bản', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop&q=80', 20, 'Thịt bò thái lát xào hành tây mềm mọng', 145000, 95000, 3000, 'Đề Xuất', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 5),
(21, 'Trà Sữa Oolong Nướng Trân Châu Hoàng Kim', '15 Tôn Thất Tùng, Quận 1', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=600&auto=format&fit=crop&q=80', 10, 'Vị trà rang thơm nồng quyện sữa béo bùi', 55000, 29000, 2000, 'Giảm 48%', TRUE, NOW(), '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 7),
(22, 'Trà Đào Cam Sả Tươi Mát Giải Nhiệt', '15 Tôn Thất Tùng, Quận 1', 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=600&auto=format&fit=crop&q=80', 10, 'Đào miếng giòn ngọt kết hợp sả thơm ngát', 45000, 28000, 2000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 7),
(23, 'Sữa Tươi Trân Châu Đường Đen Đài Loan', '15 Tôn Thất Tùng, Quận 1', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80', 10, 'Trân châu nấu đường đen dẻo dai ấm nóng', 50000, 32000, 2000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 7),
(24, 'Cà Phê Muối Kem Béo Cổ Điển', '15 Tôn Thất Tùng, Quận 1', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80', 10, 'Cà phê pha phin truyền thống lớp kem muối mằn mặn', 40000, 25000, 2000, 'Yêu Thích', TRUE, NOW(), '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 7),
(25, 'Bánh Mì Heo Quay Giòn Bì Kèm Dưa Leo', '123 Nguyễn Thị Minh Khai, Quận 1', 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', 10, 'Bánh mì giòn rụm heo quay da giòn nước sốt đậm đà', 35000, 22000, 1000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 8),
(26, 'Bánh Mì Chảo Bò Né Trứng Xúc Xích Phô Mai', '45 Lê Duẩn, Bến Nghé, Quận 1', 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600&auto=format&fit=crop&q=80', 15, 'Chảo bò bốc khói sốt bơ trứng béo ngậy', 55000, 38000, 2000, 'Nổi Bật', TRUE, NOW(), '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 8),
(27, 'Salad Ức Gà Áp Chảo Sốt Mè Rang Nhật', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80', 15, 'Rau củ tươi giòn thanh lọc cơ thể ít calo', 75000, 48000, 2000, 'Eat Clean', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 9),
(28, 'Salad Bò Úc Sốt Giấm Balsamic', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop&q=80', 15, 'Thịt bò Úc mềm ngọt kết hợp giấm đen Ý', 95000, 65000, 3000, 'Đề Xuất', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 9),
(29, 'Combo Lẩu Thái Tomyum Hải Sản 2 Người', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=600&auto=format&fit=crop&q=80', 30, 'Nước lẩu chua cay chuẩn vị Thái tôm mực bò nấm', 250000, 139000, 5000, 'Giảm 45%', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 10),
(30, 'Cơm Trộn Hàn Quốc Bibimbap Thố Đá', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1553163147-622ab57be1c7?w=600&auto=format&fit=crop&q=80', 20, 'Rau củ ngũ sắc thịt bò trứng lòng đào sốt gochujang', 85000, 55000, 3000, 'Bán Chạy', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 6),
(31, 'Gà Sốt Cay Phô Mai Kéo Sợi Hàn Quốc', '50 Nguyễn Đình Chiểu, Quận 1', 'https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=600&auto=format&fit=crop&q=80', 20, 'Thịt gà rút xương thấm đẫm sốt cay phủ phô mai Mozzarella', 135000, 85000, 3000, 'Giảm Sốc', TRUE, NOW(), '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 6),
(32, 'Mì Cay Hàn Quốc Hải Sản 7 Cấp Độ', '220 Pasteur, Phường Võ Thị Sáu, Quận 3', 'https://images.unsplash.com/photo-1612927601601-6638404737ce?w=600&auto=format&fit=crop&q=80', 15, 'Sợi mì Koreno dai ngon tôm mực xúc xích bò viên', 65000, 42000, 2000, 'Yêu Thích', TRUE, NOW(), '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 6);

-- ====================================================================
-- 3. THÊM HÌNH ẢNH MẪU CHO MÓN ĂN (food_images)
-- ====================================================================
INSERT INTO food_images (id, image_url, is_primary, food_id) VALUES
(1, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80', TRUE, 1),
(2, 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80', FALSE, 1),
(3, 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80', TRUE, 2),
(4, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80', FALSE, 2),
(5, 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=600&auto=format&fit=crop&q=80', TRUE, 3),
(6, 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80', FALSE, 3),
(7, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop&q=80', TRUE, 4),
(8, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80', FALSE, 4),
(9, 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=600&auto=format&fit=crop&q=80', TRUE, 5),
(10, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', FALSE, 5),
(11, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', TRUE, 6),
(12, 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=600&auto=format&fit=crop&q=80', FALSE, 6),
(13, 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=600&auto=format&fit=crop&q=80', TRUE, 7),
(14, 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', FALSE, 7),
(15, 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', TRUE, 8),
(16, 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=600&auto=format&fit=crop&q=80', FALSE, 8),
(17, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80', TRUE, 9),
(18, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&auto=format&fit=crop&q=80', FALSE, 9),
(19, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&auto=format&fit=crop&q=80', TRUE, 10),
(20, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80', FALSE, 10),
(21, 'https://images.unsplash.com/photo-1621996346565-e3d5d6281691?w=600&auto=format&fit=crop&q=80', TRUE, 11),
(22, 'https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=600&auto=format&fit=crop&q=80', FALSE, 11),
(23, 'https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=600&auto=format&fit=crop&q=80', TRUE, 12),
(24, 'https://images.unsplash.com/photo-1621996346565-e3d5d6281691?w=600&auto=format&fit=crop&q=80', FALSE, 12),
(25, 'https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=600&auto=format&fit=crop&q=80', TRUE, 13),
(26, 'https://images.unsplash.com/photo-1562967914-608f82629710?w=600&auto=format&fit=crop&q=80', FALSE, 13),
(27, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80', TRUE, 14),
(28, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&auto=format&fit=crop&q=80', FALSE, 14),
(29, 'https://images.unsplash.com/photo-1562967914-608f82629710?w=600&auto=format&fit=crop&q=80', TRUE, 15),
(30, 'https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=600&auto=format&fit=crop&q=80', FALSE, 15),
(31, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&auto=format&fit=crop&q=80', TRUE, 16),
(32, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80', FALSE, 16),
(33, 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80', TRUE, 17),
(34, 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=600&auto=format&fit=crop&q=80', FALSE, 17),
(35, 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=600&auto=format&fit=crop&q=80', TRUE, 18),
(36, 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80', FALSE, 18),
(37, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', TRUE, 19),
(38, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop&q=80', FALSE, 19),
(39, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop&q=80', TRUE, 20),
(40, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', FALSE, 20),
(41, 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=600&auto=format&fit=crop&q=80', TRUE, 21),
(42, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=600&auto=format&fit=crop&q=80', FALSE, 21),
(43, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=600&auto=format&fit=crop&q=80', TRUE, 22),
(44, 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=600&auto=format&fit=crop&q=80', FALSE, 22),
(45, 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80', TRUE, 23),
(46, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80', FALSE, 23),
(47, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80', TRUE, 24),
(48, 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80', FALSE, 24),
(49, 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', TRUE, 25),
(50, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600&auto=format&fit=crop&q=80', FALSE, 25),
(51, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600&auto=format&fit=crop&q=80', TRUE, 26),
(52, 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80', FALSE, 26),
(53, 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80', TRUE, 27),
(54, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop&q=80', FALSE, 27),
(55, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop&q=80', TRUE, 28),
(56, 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80', FALSE, 28),
(57, 'https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=600&auto=format&fit=crop&q=80', TRUE, 29),
(58, 'https://images.unsplash.com/photo-1553163147-622ab57be1c7?w=600&auto=format&fit=crop&q=80', FALSE, 29),
(59, 'https://images.unsplash.com/photo-1553163147-622ab57be1c7?w=600&auto=format&fit=crop&q=80', TRUE, 30),
(60, 'https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=600&auto=format&fit=crop&q=80', FALSE, 30),
(61, 'https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=600&auto=format&fit=crop&q=80', TRUE, 31),
(62, 'https://images.unsplash.com/photo-1612927601601-6638404737ce?w=600&auto=format&fit=crop&q=80', FALSE, 31),
(63, 'https://images.unsplash.com/photo-1612927601601-6638404737ce?w=600&auto=format&fit=crop&q=80', TRUE, 32),
(64, 'https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=600&auto=format&fit=crop&q=80', FALSE, 32);

-- ====================================================================
-- 4. THÊM LƯỢT THÍCH MÓN ĂN VÀ QUÁN ĂN MẪU (food_likes, merchant_likes)
-- ====================================================================
INSERT INTO food_likes (user_id, food_id) VALUES
('00000000-0000-0000-0000-000000000002', 1),
('00000000-0000-0000-0000-000000000002', 3),
('00000000-0000-0000-0000-000000000002', 5),
('00000000-0000-0000-0000-000000000002', 9),
('00000000-0000-0000-0000-000000000002', 13),
('00000000-0000-0000-0000-000000000002', 21),
('00000000-0000-0000-0000-000000000003', 1),
('00000000-0000-0000-0000-000000000003', 2),
('00000000-0000-0000-0000-000000000003', 5),
('00000000-0000-0000-0000-000000000003', 10),
('00000000-0000-0000-0000-000000000003', 13),
('00000000-0000-0000-0000-000000000003', 17),
('00000000-0000-0000-0000-000000000003', 21),
('00000000-0000-0000-0000-000000000004', 1),
('00000000-0000-0000-0000-000000000004', 5),
('00000000-0000-0000-0000-000000000004', 9),
('00000000-0000-0000-0000-000000000004', 21),
('00000000-0000-0000-0000-000000000005', 1),
('00000000-0000-0000-0000-000000000005', 3),
('00000000-0000-0000-0000-000000000005', 13),
('00000000-0000-0000-0000-000000000005', 25);

INSERT INTO merchant_likes (user_id, merchant_id) VALUES
('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000005');