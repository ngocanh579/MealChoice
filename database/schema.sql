drop database meal_choice ;
create database meal_choice;
use meal_choice ;

create table roles (
                       id bigint primary key auto_increment,
                       name varchar(50) unique not null
);

create table users (
                       id bigint primary key auto_increment,
                       name varchar(100),
                       email varchar(100) unique not null ,
                       phone_number varchar(20) unique not null,
                       password varchar(255) not null ,
                       date_of_birth date,
                       gender varchar(20),
                       is_active boolean default true
);

create table user_roles (
                            user_id bigint,
                            role_id bigint,
                            primary key(user_id, role_id),
                            foreign key(user_id) references users(id),
                            foreign key(role_id) references roles(id)
);

create table merchants (
                           id bigint primary key auto_increment,
                           user_id bigint unique,
                           restaurant_name varchar(150) not null,
                           email varchar(100) unique not null,
                           phone varchar(20)  unique not null,
                           address varchar(255) not null,
                           open_time time,
                           close_time time,
                           merchant_status varchar(30) default 'PENDING',
                           foreign key (user_id) references users(id)
);
-- Roles
INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_USER'),
                             ('ROLE_MERCHANT');
-- Users
INSERT INTO users
(name, email, phone_number, password, date_of_birth, gender, is_active)
VALUES
    ('Admin','admin@mealchoice.com','0900000001','hash','1990-01-01','MALE',true),
    ('Nguyen Van An','an@gmail.com','0900000002','hash','2000-05-10','MALE',true),
    ('Tran Thi Binh','binh@gmail.com','0900000003','hash','2001-08-15','FEMALE',true),
    ('Le Van Nam','nam@merchant.com','0900000004','hash','1995-03-20','MALE',true),
    ('Pham Thi Hoa','hoa@merchant.com','0900000005','hash','1996-11-25','FEMALE',true),
    ('Nguyen Van Minh','minh@merchant.com','0900000006','hash','1994-02-02','MALE',true),
    ('Do Thi Lan','lan@merchant.com','0900000007','hash','1997-06-06','FEMALE',true),
    ('Hoang Duc','duc@merchant.com','0900000008','hash','1993-09-09','MALE',true);

-- User Roles
INSERT INTO user_roles(user_id, role_id) VALUES
                                             (1,1),
                                             (2,2),
                                             (3,2),
                                             (4,2),(4,3),
                                             (5,2),(5,3),
                                             (6,2),(6,3),
                                             (7,2),(7,3),
                                             (8,2),(8,3);

-- Merchants
INSERT INTO merchants
(user_id, restaurant_name, email, phone, address, open_time, close_time, merchant_status)
VALUES
    (4,'Quan Com Ga Nam','nam@merchant.com','0900000004','Ha Noi','08:00:00','22:00:00','APPROVED'),
    (5,'Bep Nha Hoa','hoa@merchant.com','0900000005','Ho Chi Minh','09:00:00','21:30:00','PENDING'),
    (6,'Pizza Y Ngon','minh@merchant.com','0900000006','Da Nang','10:00:00','23:00:00','APPROVED'),
    (7,'Tra Sua Moc','lan@merchant.com','0900000007','Hai Phong','08:30:00','22:30:00','REJECTED'),
    (8,'Bun Cha Ha Thanh','duc@merchant.com','0900000008','Ha Noi','07:00:00','20:00:00','BLOCKED');