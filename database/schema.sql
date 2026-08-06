-- Active: 1785914295113@@mysql-22176-buivietbacn01-1ff7.a.aivencloud.com@10055@meal_choice
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
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(60) NOT NULL,
    display_name VARCHAR(32) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    avatar_url VARCHAR(255),
    dob DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6)
);

-- User Roles
CREATE TABLE user_roles (
    user_id VARCHAR(36),
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Merchants
CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE,
    merchant_restaurant_name VARCHAR(150) NOT NULL,
    merchant_email VARCHAR(255) UNIQUE NOT NULL,
    merchant_phone VARCHAR(20) UNIQUE NOT NULL,
    merchant_status VARCHAR(30) DEFAULT 'PENDING',
    is_trusted_partner BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- MERCHANT ADDRESSES

CREATE TABLE merchant_addresses (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_address VARCHAR(255) NOT NULL,
    merchant_open_time TIME,
    merchant_close_time TIME,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id)
);

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE merchant_addresses;

TRUNCATE TABLE user_roles;

TRUNCATE TABLE merchants;

TRUNCATE TABLE users;

TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO
    roles (id, name)
VALUES (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_MERCHANT');

INSERT INTO
    users (
        id,
        email,
        password,
        display_name,
        phone_number,
        gender,
        avatar_url,
        dob,
        is_active,
        created_at
    )
VALUES (
        '00000000-0000-0000-0000-000000000001',
        'admin@mealchoice.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Admin',
        '0900000001',
        'MALE',
        NULL,
        '1990-01-01',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'an@gmail.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Nguyen Van An',
        '0900000002',
        'MALE',
        NULL,
        '2000-05-10',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'binh@gmail.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Tran Thi Binh',
        '0900000003',
        'FEMALE',
        NULL,
        '2001-08-15',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        'nam@merchant.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Le Van Nam',
        '0900000004',
        'MALE',
        NULL,
        '1995-03-20',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        'hoa@merchant.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Pham Thi Hoa',
        '0900000005',
        'FEMALE',
        NULL,
        '1996-11-25',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        'minh@merchant.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Nguyen Van Minh',
        '0900000006',
        'MALE',
        NULL,
        '1994-02-02',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        'lan@merchant.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Do Thi Lan',
        '0900000007',
        'FEMALE',
        NULL,
        '1997-06-06',
        TRUE,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        'duc@merchant.com',
        '$2a$10$JUx/hVBylvDiS4ctiYi6hubPy4n9qKQpx43ayQprX64kcLbHUaOdO',
        'Hoang Duc',
        '0900000008',
        'MALE',
        NULL,
        '1993-09-09',
        TRUE,
        NOW()
    );

INSERT INTO
    user_roles (user_id, role_id)
VALUES (
        '00000000-0000-0000-0000-000000000001',
        1
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        3
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        3
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        3
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        3
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        2
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        3
    );

INSERT INTO
    merchants (
        id,
        user_id,
        merchant_restaurant_name,
        merchant_email,
        merchant_phone,
        merchant_status,
        is_trusted_partner
    )
VALUES (
        '10000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000004',
        'Quan Com Ga Nam',
        'nam@merchant.com',
        '0900000004',
        'APPROVED',
        TRUE
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000005',
        'Bep Nha Hoa',
        'hoa@merchant.com',
        '0900000005',
        'PENDING',
        FALSE
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-000000000006',
        'Pizza Y Ngon',
        'minh@merchant.com',
        '0900000006',
        'APPROVED',
        FALSE
    ),
    (
        '10000000-0000-0000-0000-000000000004',
        '00000000-0000-0000-0000-000000000007',
        'Tra Sua Moc',
        'lan@merchant.com',
        '0900000007',
        'REJECTED',
        FALSE
    ),
    (
        '10000000-0000-0000-0000-000000000005',
        '00000000-0000-0000-0000-000000000008',
        'Bun Cha Ha Thanh',
        'duc@merchant.com',
        '0900000008',
        'BLOCKED',
        FALSE
    );