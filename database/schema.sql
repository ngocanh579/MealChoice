create database meal_choice;
use meal_choice ;

create table roles (
id bigint primary key auto_increment,
name varchar(50) not null
);

create table users (
id bigint primary key auto_increment,
name_user varchar(100),
email_user varchar(100) unique not null ,
password_user varchar(255) not null ,
phone varchar(20),
birthday date,
gender varchar(20),
enabled boolean default false ,
role_id bigint ,
foreign key (role_id) references roles (id)
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

alter table roles modify column name varchar(50) unique not null;

INSERT INTO roles (name) VALUES
('ADMIN'),
('USER'),
('MERCHANT');

INSERT INTO users 
(name_user,email_user,password_user,phone,birthday,gender,enabled,role_id)
VALUES
('Admin System','admin@mealchoice.com','hash','0900000001','1990-01-01','MALE',true,1),
('Nguyen Van An','an@gmail.com','hash','0900000002','2000-05-10','MALE',true,2),
('Tran Thi Binh','binh@gmail.com','hash','0900000003','2001-08-15','FEMALE',true,2),
('Le Van Nam','nam@merchant.com','hash','0900000004','1995-03-20','MALE',true,3),
('Pham Thi Hoa','hoa@merchant.com','hash','0900000005','1996-11-25','FEMALE',true,3),
('Nguyen Van Minh','minh@merchant.com','hash','0900000006','1994-02-02','MALE',true,3),
('Do Thi Lan','lan@merchant.com','hash','0900000007','1997-06-06','FEMALE',true,3),
('Hoang Duc','duc@merchant.com','hash','0900000008','1993-09-09','MALE',true,3);

INSERT INTO merchants
(user_id,restaurant_name,email,phone,address,open_time,close_time,merchant_status)
VALUES
(4,'Quan Com Ga Nam','nam@merchant.com','0900000004','Ha Noi','08:00:00','22:00:00','APPROVED'),
(5,'Bep Nha Hoa','hoa@merchant.com','0900000005','Ho Chi Minh','09:00:00','21:30:00','PENDING'),
(6,'Pizza Y Ngon','minh@merchant.com','0900000006','Da Nang','10:00:00','23:00:00','APPROVED'),
(7,'Tra Sua Moc','lan@merchant.com','0900000007','Hai Phong','08:30:00','22:30:00','REJECTED'),
(8,'Bun Cha Ha Thanh','duc@merchant.com','0900000008','Ha Noi','07:00:00','20:00:00','BLOCKED');