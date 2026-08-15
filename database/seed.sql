USE meal_choice;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE merchant_likes;
TRUNCATE TABLE food_likes;
TRUNCATE TABLE food_images;
TRUNCATE TABLE food_tag_mapping;
TRUNCATE TABLE food_category_mapping;
TRUNCATE TABLE foods;
TRUNCATE TABLE merchant_addresses;
TRUNCATE TABLE merchants;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE tags;
TRUNCATE TABLE food_categories;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;

-- Roles
INSERT INTO roles (id, name) VALUES
                                 (1,'ROLE_ADMIN'),(2,'ROLE_USER'),(3,'ROLE_MERCHANT');

-- Users
INSERT INTO users (id,email,password,display_name,phone_number,gender,avatar_url,dob,is_active,created_at) VALUES
                                                                                                               ('00000000-0000-0000-0000-000000000001','admin@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Admin','0900000001','MALE',NULL,'1995-01-01',1,NOW()),
                                                                                                               ('00000000-0000-0000-0000-000000000002','user@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Nguyễn Văn User','0900000002','MALE',NULL,'2000-05-10',1,NOW()),
                                                                                                               ('00000000-0000-0000-0000-000000000003','merchant@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Merchant Demo','0900000003','MALE',NULL,'1998-08-15',1,NOW());

-- User Roles
INSERT INTO user_roles (user_id,role_id) VALUES
                                             ('00000000-0000-0000-0000-000000000001',1),
                                             ('00000000-0000-0000-0000-000000000002',2),
                                             ('00000000-0000-0000-0000-000000000003',3);

-- Merchants
INSERT INTO merchants (id,user_id,merchant_restaurant_name,merchant_email,merchant_phone,merchant_status,is_trusted_partner) VALUES
                                                                                                                                 ('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000003','Quán Ngon Hà Nội','merchant@gmail.com','0911111111','APPROVED',1),
                                                                                                                                 ('10000000-0000-0000-0000-000000000002',NULL,'Bếp Nhà Mình','bepnhaminh@gmail.com','0911111112','PENDING',0);

-- Merchant Addresses
INSERT INTO merchant_addresses (id,merchant_id,merchant_address,province_code,district_code,ward_code,merchant_open_time,merchant_close_time,is_default) VALUES
                                                                                                                                                             ('20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','25 Hàng Bông','01','001','00001','08:00:00','22:00:00',1),
                                                                                                                                                             ('20000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','50 Nguyễn Trãi','01','002','00002','09:00:00','21:00:00',1);

-- Food Categories
INSERT INTO food_categories (id,category_name,category_description) VALUES
                                                                        (1,'Món Việt','Các món ăn truyền thống Việt Nam'),
                                                                        (2,'Món Nhật','Các món ăn Nhật Bản'),
                                                                        (3,'Đồ uống','Nước uống và đồ giải khát'),
                                                                        (4,'Đồ ăn nhanh','Các món ăn nhanh');

-- Tags
INSERT INTO tags (id,tag_name) VALUES
                                   (1,'Bán chạy'),(2,'Món mới'),(3,'Giảm giá'),(4,'Đề xuất');

-- Foods
INSERT INTO foods (id,merchant_id,merchant_address_id,food_name,preparation_time,food_note,price,discount_price,service_fee,views,order_count,is_active,is_recommended,created_at) VALUES
                                                                                                                                                                                       (1,'10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Phở Bò',15,'Phở bò truyền thống',55000,45000,5000,120,35,1,1,NOW()),
                                                                                                                                                                                       (2,'10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Bún Chả',20,'Bún chả Hà Nội',50000,40000,5000,98,28,1,1,NOW()),
                                                                                                                                                                                       (3,'10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Cơm Rang Dưa Bò',15,'Cơm rang thơm ngon',45000,NULL,5000,75,20,1,0,NOW()),
                                                                                                                                                                                       (4,'10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Trà Đào Cam Sả',10,'Đồ uống mát lạnh',35000,30000,3000,150,45,1,1,NOW()),
                                                                                                                                                                                       (5,'10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','Nem Rán',15,'Nem rán giòn',40000,NULL,3000,60,15,1,0,NOW());

-- Food Category Mapping
INSERT INTO food_category_mapping (food_id,food_category_id) VALUES
                                                                 (1,1),(2,1),(3,1),(4,3),(5,1);

-- Food Tag Mapping
INSERT INTO food_tag_mapping (food_id,tag_id) VALUES
                                                  (1,1),(1,4),(2,1),(2,3),(4,2),(4,4),(5,3);

-- Food Images
INSERT INTO food_images (id,food_id,image_url,is_primary) VALUES
                                                              (1,1,'/uploads/foods/pho-bo.jpg',1),
                                                              (2,2,'/uploads/foods/bun-cha.jpg',1),
                                                              (3,3,'/uploads/foods/com-rang.jpg',1),
                                                              (4,4,'/uploads/foods/tra-dao.jpg',1),
                                                              (5,5,'/uploads/foods/nem-ran.jpg',1);

-- Food Likes
INSERT INTO food_likes (user_id,food_id) VALUES
                                             ('00000000-0000-0000-0000-000000000002',1),
                                             ('00000000-0000-0000-0000-000000000002',2),
                                             ('00000000-0000-0000-0000-000000000002',4);

-- Merchant Likes
INSERT INTO merchant_likes (user_id,merchant_id) VALUES
    ('00000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001');

SET FOREIGN_KEY_CHECKS = 1;