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

-- 5. MERCHANT ADDRESSES

CREATE TABLE merchant_addresses (
                                    id VARCHAR(36) PRIMARY KEY,

                                    merchant_id VARCHAR(36) NOT NULL,
                                    merchant_address VARCHAR(255) NOT NULL,

    -- Địa chỉ hành chính mới
                                    province_code VARCHAR(20) NOT NULL,
                                    district_code VARCHAR(20) NOT NULL,
                                    ward_code VARCHAR(20) NOT NULL,

                                    merchant_open_time TIME  NULL,
                                    merchant_close_time TIME  NULL,

                                    is_default BOOLEAN NOT NULL DEFAULT FALSE,

                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

                                    FOREIGN KEY (merchant_id)
                                        REFERENCES merchants(id)

);

-- category
CREATE TABLE food_categories (
                                 id VARCHAR(36) PRIMARY KEY,
                                 category_name VARCHAR(100) NOT NULL UNIQUE,
                                 category_description VARCHAR(255),
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- food
CREATE TABLE foods (
                       id VARCHAR(36) PRIMARY KEY,

                       merchant_id VARCHAR(36) NOT NULL,
                       merchant_address_id VARCHAR(36) NOT NULL,
                       food_category_id VARCHAR(36) NOT NULL,

                       food_name VARCHAR(150) NOT NULL,
                       preparation_time INT,
                       food_note TEXT,

                       price DECIMAL(12,2) NOT NULL,
                       discount_price DECIMAL(12,2),
                       service_fee DECIMAL(12,2) DEFAULT 0,

                       views INT NOT NULL DEFAULT 0,
                       order_count INT NOT NULL DEFAULT 0,

                       is_recommended BOOLEAN NOT NULL DEFAULT FALSE,

                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at DATETIME NULL,

                       FOREIGN KEY (merchant_id) REFERENCES merchants(id),
                       FOREIGN KEY (merchant_address_id) REFERENCES merchant_addresses(id),
                       FOREIGN KEY (food_category_id) REFERENCES food_categories(id)

);

-- images
CREATE TABLE food_images (
                             id VARCHAR(36) PRIMARY KEY,
                             food_id VARCHAR(36) NOT NULL,
                             image_url VARCHAR(500) NOT NULL,
                             is_primary BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                             FOREIGN KEY (food_id) REFERENCES foods(id)

);