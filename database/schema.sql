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
    user_id VARCHAR(36) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
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

-- Merchants
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
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Merchant Addresses
CREATE TABLE merchant_addresses (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_address VARCHAR(255) NOT NULL,
    province_code VARCHAR(20) NOT NULL,
    district_code VARCHAR(20) NOT NULL,
    ward_code VARCHAR(20) NOT NULL,
    merchant_open_time TIME NULL,
    merchant_close_time TIME NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id)
);

-- Food Categories
CREATE TABLE food_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    category_description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tags
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Foods
CREATE TABLE foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_address_id VARCHAR(36) NOT NULL,
    food_name VARCHAR(150) NOT NULL,
    preparation_time INT,
    food_note TEXT,
    price DECIMAL(12, 2) NOT NULL,
    discount_price DECIMAL(12, 2),
    service_fee DECIMAL(12, 2) DEFAULT 0,
    views INT NOT NULL DEFAULT 0,
    order_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id),
    FOREIGN KEY (merchant_address_id) REFERENCES merchant_addresses (id)
);

-- Food Category Mapping
CREATE TABLE food_category_mapping (
    food_id BIGINT NOT NULL,
    food_category_id BIGINT NOT NULL,
    PRIMARY KEY (food_id, food_category_id),
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE,
    FOREIGN KEY (food_category_id) REFERENCES food_categories (id) ON DELETE CASCADE
);

-- Food Tag Mapping
CREATE TABLE food_tag_mapping (
    food_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (food_id, tag_id),
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- Food Images
CREATE TABLE food_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    food_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE
);

-- Food Likes
CREATE TABLE food_likes (
    user_id VARCHAR(36) NOT NULL,
    food_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, food_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE CASCADE
);

-- Merchant Likes
CREATE TABLE merchant_likes (
    user_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, merchant_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (merchant_id) REFERENCES merchants (id) ON DELETE CASCADE
);