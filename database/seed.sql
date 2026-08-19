USE meal_choice;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE merchant_likes;
TRUNCATE TABLE food_likes;
TRUNCATE TABLE food_images;
TRUNCATE TABLE food_tag_mapping;
TRUNCATE TABLE food_category_mapping;
TRUNCATE TABLE foods;
TRUNCATE TABLE tags;
TRUNCATE TABLE food_categories;
TRUNCATE TABLE merchant_addresses;
TRUNCATE TABLE merchants;
TRUNCATE TABLE addresses;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Roles
INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_ADMIN'),
                                 (2, 'ROLE_USER'),
                                 (3, 'ROLE_MERCHANT');

-- 2. Users (Password mặc định cho tất cả tài khoản: 123456)
INSERT INTO users (id, email, password, display_name, phone_number, gender, avatar_url, dob, is_active, created_at) VALUES
('00000000-0000-0000-0000-000000000001', 'admin@gmail.com', '$2a$10$JhjAsJnlV0WGotKDMSl2b.st8YryfvIAQHl5RfHmyx/Des805kDLG', 'Quản Trị Viên', '0900000001', 'MALE', 'https://api.dicebear.com/7.x/bottts/svg?seed=Admin', '1995-01-01', TRUE, NOW()),
('00000000-0000-0000-0000-000000000002', 'anh2k3le@gmail.com', '$2a$10$JhjAsJnlV0WGotKDMSl2b.st8YryfvIAQHl5RfHmyx/Des805kDLG', 'Nguyễn Văn Anh', '0900000002', 'MALE', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Anh', '2000-05-10', TRUE, NOW()),
('00000000-0000-0000-0000-000000000003', 'user2@gmail.com', '$2a$10$JhjAsJnlV0WGotKDMSl2b.st8YryfvIAQHl5RfHmyx/Des805kDLG', 'Trần Thị Bình', '0900000003', 'FEMALE', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Binh', '1999-11-20', TRUE, NOW()),
('00000000-0000-0000-0000-000000000004', 'merchant1@gmail.com', '$2a$10$JhjAsJnlV0WGotKDMSl2b.st8YryfvIAQHl5RfHmyx/Des805kDLG', 'Chủ Quán Bún Cả', '0900000004', 'MALE', NULL, '1988-08-15', TRUE, NOW()),
('00000000-0000-0000-0000-000000000005', 'merchant2@gmail.com', '$2a$10$JhjAsJnlV0WGotKDMSl2b.st8YryfvIAQHl5RfHmyx/Des805kDLG', 'Chủ Quán Trà Sữa', '0900000005', 'FEMALE', NULL, '1992-03-12', TRUE, NOW());

-- 3. User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
                                              ('00000000-0000-0000-0000-000000000001', 1),
                                              ('00000000-0000-0000-0000-000000000002', 2),
                                              ('00000000-0000-0000-0000-000000000003', 2),
                                              ('00000000-0000-0000-0000-000000000004', 3),
                                              ('00000000-0000-0000-0000-000000000005', 3);

-- 4. User Addresses
INSERT INTO addresses (id, contact_name, contact_phone, city, district, ward, street, note, is_default, user_id) VALUES
                                                                                                                     (1, 'Nguyễn Văn Anh', '0900000002', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', 'Số 12 Ngõ 68 Cầu Giấy', 'Cổng màu xanh', TRUE, '00000000-0000-0000-0000-000000000002'),
                                                                                                                     (2, 'Nguyễn Văn Anh (Cơ quan)', '0900000002', 'Hà Nội', 'Quận Nam Từ Liêm', 'Phường Mỹ Đình 1', 'Tòa nhà Sông Đà, Phạm Hùng', 'Gọi trước khi giao', FALSE, '00000000-0000-0000-0000-000000000002'),
                                                                                                                     (3, 'Trần Thị Bình', '0900000003', 'Hà Nội', 'Quận Đống Đa', 'Phường Láng Hạ', 'Số 45 Láng Hạ', 'Gửi bảo vệ tòa nhà', TRUE, '00000000-0000-0000-0000-000000000003');

-- 5. Merchants
INSERT INTO merchants (id, user_id, merchant_restaurant_name, merchant_email, merchant_phone, merchant_status, lock_reason, locked_at, reject_reason, rejected_at, is_trusted_partner) VALUES
                                                                                                                                                                                           ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', 'Quán Ngon Hà Nội - Phở & Bún Chả', 'merchant1@gmail.com', '0911111111', 'APPROVED', NULL, NULL, NULL, NULL, TRUE),
                                                                                                                                                                                           ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', 'Tiệm Trà & Ăn Vặt Chill', 'merchant2@gmail.com', '0911111112', 'APPROVED', NULL, NULL, NULL, NULL, FALSE);

-- 6. Merchant Addresses
INSERT INTO merchant_addresses (id, merchant_id, merchant_address, province_code, district_code, ward_code, merchant_open_time, merchant_close_time, is_default, created_at, updated_at) VALUES
                                                                                                                                                                                             ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '25 Hàng Bông, Phường Hàng Bông', '01', '001', '00001', '06:30:00', '22:00:00', TRUE, NOW(), NOW()),
                                                                                                                                                                                             ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '102 Chùa Láng, Phường Láng Thượng', '01', '006', '00184', '08:00:00', '23:00:00', TRUE, NOW(), NOW());

-- 7. Food Categories
INSERT INTO food_categories (id, category_name, category_description, created_at, updated_at) VALUES
                                                                                                  (1, 'Cơm & Món Việt', 'Các món ăn truyền thống đậm đà vị Việt', NOW(), NOW()),
                                                                                                  (2, 'Bún / Phở / Mì', 'Món nước và bún trộn nóng hổi', NOW(), NOW()),
                                                                                                  (3, 'Đồ Uống & Trà Sữa', 'Nước giải khát, trà trái cây và trà sữa', NOW(), NOW()),
                                                                                                  (4, 'Ăn Vặt & Món Phụ', 'Các món ăn kèm, đồ chiên giòn rụm', NOW(), NOW());

-- 8. Tags
INSERT INTO tags (id, tag_name, created_at, updated_at) VALUES
                                                            (1, 'Bán chạy', NOW(), NOW()),
                                                            (2, 'Món mới', NOW(), NOW()),
                                                            (3, 'Giảm giá cực sốc', NOW(), NOW()),
                                                            (4, 'Gợi ý hôm nay', NOW(), NOW()),
                                                            (5, 'Freeship', NOW(), NOW());

-- 9. Foods
INSERT INTO foods (id, merchant_id, merchant_address_id, food_name, preparation_time, food_note, price, discount_price, service_fee, views, order_count, is_active, is_recommended, created_at, updated_at, deleted_at) VALUES
                                                                                                                                                                                                                            (1, '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Phở Bò Tái Nạm', 15, 'Nước dùng ninh từ xương ống 12 tiếng, kèm rau sống và quẩy', 60000.00, 50000.00, 5000.00, 320, 145, TRUE, TRUE, NOW(), NOW(), NULL),
                                                                                                                                                                                                                            (2, '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Bún Chả Hà Nội Đặc Biệt', 20, 'Chả nướng than hoa thơm lừng, đu đủ muối giòn', 55000.00, 45000.00, 5000.00, 280, 98, TRUE, TRUE, NOW(), NOW(), NULL),
                                                                                                                                                                                                                            (3, '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Cơm Rang Dưa Bò', 15, 'Cơm hạt tơi giòn, dưa chua xào thịt bò mềm', 50000.00, NULL, 5000.00, 110, 42, TRUE, FALSE, NOW(), NOW(), NULL),
                                                                                                                                                                                                                            (4, '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Trà Đào Cam Sả Tươi', 10, 'Đồ uống giải nhiệt kèm 3 miếng đào giòn', 38000.00, 29000.00, 3000.00, 500, 210, TRUE, TRUE, NOW(), NOW(), NULL),
                                                                                                                                                                                                                            (5, '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Trà Sữa Trân Châu Đường Đen', 10, 'Độ ngọt vừa phải, trân châu dẻo mềm', 42000.00, NULL, 3000.00, 410, 185, TRUE, TRUE, NOW(), NOW(), NULL),
                                                                                                                                                                                                                            (6, '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Nem Rán Hà Nội (5 Chiếc)', 15, 'Nem nhân thịt mộc nhĩ giòn rụm', 35000.00, NULL, 3000.00, 95, 30, TRUE, FALSE, NOW(), NOW(), NULL);

-- 10. Food Category Mapping
INSERT INTO food_category_mapping (food_id, food_category_id) VALUES
                                                                  (1, 2), -- Phở Bò -> Bún / Phở / Mì
                                                                  (2, 2), -- Bún Chả -> Bún / Phở / Mì
                                                                  (3, 1), -- Cơm Rang -> Cơm & Món Việt
                                                                  (4, 3), -- Trà Đào -> Đồ Uống
                                                                  (5, 3), -- Trà Sữa -> Đồ Uống
                                                                  (6, 4); -- Nem Rán -> Ăn Vặt

-- 11. Food Tag Mapping
INSERT INTO food_tag_mapping (food_id, tag_id) VALUES
                                                   (1, 1), (1, 4),        -- Phở: Bán chạy, Gợi ý
                                                   (2, 1), (2, 3),        -- Bún Chả: Bán chạy, Giảm giá
                                                   (4, 1), (4, 3), (4, 5),-- Trà Đào: Bán chạy, Giảm giá, Freeship
                                                   (5, 1), (5, 4),        -- Trà Sữa: Bán chạy, Gợi ý
                                                   (6, 2);                -- Nem Rán: Món mới

-- 12. Food Images
INSERT INTO food_images (id, food_id, image_url, is_primary, created_at) VALUES
                                                                             (1, 1, 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43', TRUE, NOW()),
                                                                             (2, 2, 'https://images.unsplash.com/photo-1569058242253-92a9c755a0ec', TRUE, NOW()),
                                                                             (3, 3, 'https://images.unsplash.com/photo-1603133872878-684f208fb84b', TRUE, NOW()),
                                                                             (4, 4, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc', TRUE, NOW()),
                                                                             (5, 5, 'https://images.unsplash.com/photo-1572490122747-3968b75cc699', TRUE, NOW()),
                                                                             (6, 6, 'https://images.unsplash.com/photo-1541544741938-0af808871cc0', TRUE, NOW());

-- 13. Food Likes
INSERT INTO food_likes (user_id, food_id) VALUES
                                              ('00000000-0000-0000-0000-000000000002', 1),
                                              ('00000000-0000-0000-0000-000000000002', 4),
                                              ('00000000-0000-0000-0000-000000000003', 4),
                                              ('00000000-0000-0000-0000-000000000003', 5);

-- 14. Merchant Likes
INSERT INTO merchant_likes (user_id, merchant_id) VALUES
                                                      ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001'),
                                                      ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002');
