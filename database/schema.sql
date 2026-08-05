DROP DATABASE IF EXISTS meal_choice;
CREATE DATABASE meal_choice;
USE meal_choice;

-- Roles
CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- Users
CREATE TABLE users (
                       id BINARY(16) PRIMARY KEY,
                       user_email VARCHAR(255) UNIQUE,
                       user_password VARCHAR(60) NOT NULL,
                       user_display_name VARCHAR(32) NOT NULL,
                       user_phone_number VARCHAR(20) UNIQUE NOT NULL,
                       user_gender VARCHAR(20),
                       user_avatar_url VARCHAR(255),
                       user_dob DATETIME,
                       user_is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       user_created_at DATETIME NOT NULL
);

-- User Roles
CREATE TABLE user_roles (
                            user_id BINARY(16),
                            role_id BIGINT,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Merchants
CREATE TABLE merchants (
                           id BINARY(16) PRIMARY KEY,
                           user_id BINARY(16) UNIQUE,

                           merchant_restaurant_name VARCHAR(150) NOT NULL,
                           merchant_email VARCHAR(255) UNIQUE NOT NULL,
                           merchant_phone VARCHAR(20) UNIQUE NOT NULL,
                           merchant_status VARCHAR(30) DEFAULT 'PENDING',

                           FOREIGN KEY (user_id) REFERENCES users(id)
);

-- MERCHANT ADDRESSES

CREATE TABLE merchant_addresses (
                                    id BINARY(16) PRIMARY KEY,
                                    merchant_id BINARY(16) NOT NULL,

                                    merchant_address VARCHAR(255) NOT NULL,
                                    merchant_open_time TIME,
                                    merchant_close_time TIME,

                                    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE merchant_addresses;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE merchants;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_ADMIN'),
                                 (2, 'ROLE_USER'),
                                 (3, 'ROLE_MERCHANT');

INSERT INTO users
(
    id,
    user_email,
    user_password,
    user_display_name,
    user_phone_number,
    user_gender,
    user_avatar_url,
    user_dob,
    user_is_active,
    user_created_at
)
VALUES
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000001'),
        'admin@mealchoice.com',
        'hash',
        'Admin',
        '0900000001',
        'MALE',
        NULL,
        '1990-01-01 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000002'),
        'an@gmail.com',
        'hash',
        'Nguyen Van An',
        '0900000002',
        'MALE',
        NULL,
        '2000-05-10 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000003'),
        'binh@gmail.com',
        'hash',
        'Tran Thi Binh',
        '0900000003',
        'FEMALE',
        NULL,
        '2001-08-15 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000004'),
        'nam@merchant.com',
        'hash',
        'Le Van Nam',
        '0900000004',
        'MALE',
        NULL,
        '1995-03-20 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000005'),
        'hoa@merchant.com',
        'hash',
        'Pham Thi Hoa',
        '0900000005',
        'FEMALE',
        NULL,
        '1996-11-25 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000006'),
        'minh@merchant.com',
        'hash',
        'Nguyen Van Minh',
        '0900000006',
        'MALE',
        NULL,
        '1994-02-02 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000007'),
        'lan@merchant.com',
        'hash',
        'Do Thi Lan',
        '0900000007',
        'FEMALE',
        NULL,
        '1997-06-06 00:00:00',
        TRUE,
        NOW()
    ),
    (
        UUID_TO_BIN('00000000-0000-0000-0000-000000000008'),
        'duc@merchant.com',
        'hash',
        'Hoang Duc',
        '0900000008',
        'MALE',
        NULL,
        '1993-09-09 00:00:00',
        TRUE,
        NOW()
    );

INSERT INTO user_roles (user_id, role_id) VALUES
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), 1),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000003'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000004'), 3),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000005'), 3),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000006'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000006'), 3),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000007'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000007'), 3),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000008'), 2),
                                              (UUID_TO_BIN('00000000-0000-0000-0000-000000000008'), 3);

INSERT INTO merchants
(
    id,
    user_id,
    merchant_restaurant_name,
    merchant_email,
    merchant_phone,
    merchant_status
)
VALUES
    (
        UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
        UUID_TO_BIN('00000000-0000-0000-0000-000000000004'),
        'Quan Com Ga Nam',
        'nam@merchant.com',
        '0900000004',
        'APPROVED'
    ),
    (
        UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
        UUID_TO_BIN('00000000-0000-0000-0000-000000000005'),
        'Bep Nha Hoa',
        'hoa@merchant.com',
        '0900000005',
        'PENDING'
    ),
    (
        UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
        UUID_TO_BIN('00000000-0000-0000-0000-000000000006'),
        'Pizza Y Ngon',
        'minh@merchant.com',
        '0900000006',
        'APPROVED'
    ),
    (
        UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
        UUID_TO_BIN('00000000-0000-0000-0000-000000000007'),
        'Tra Sua Moc',
        'lan@merchant.com',
        '0900000007',
        'REJECTED'
    ),
    (
        UUID_TO_BIN('10000000-0000-0000-0000-000000000005'),
        UUID_TO_BIN('00000000-0000-0000-0000-000000000008'),
        'Bun Cha Ha Thanh',
        'duc@merchant.com',
        '0900000008',
        'BLOCKED'
    );

-- INSERT MERCHANT ADDRESSES

INSERT INTO merchant_addresses
(
    id,
    merchant_id,
    merchant_address,
    merchant_open_time,
    merchant_close_time
)
VALUES
    (
        UUID_TO_BIN('20000000-0000-0000-0000-000000000001'),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
        'Ha Noi',
        '08:00:00',
        '22:00:00'
    ),
    (
        UUID_TO_BIN('20000000-0000-0000-0000-000000000002'),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
        'Ho Chi Minh',
        '09:00:00',
        '21:30:00'
    ),
    (
        UUID_TO_BIN('20000000-0000-0000-0000-000000000003'),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
        'Da Nang',
        '10:00:00',
        '23:00:00'
    ),
    (
        UUID_TO_BIN('20000000-0000-0000-0000-000000000004'),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
        'Hai Phong',
        '08:30:00',
        '22:30:00'
    ),
    (
        UUID_TO_BIN('20000000-0000-0000-0000-000000000005'),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000005'),
        'Ha Noi',
        '07:00:00',
        '20:00:00'
    );