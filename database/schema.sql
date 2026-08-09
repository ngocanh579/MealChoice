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

-- DROP TABLE IF EXISTS wards;
-- DROP TABLE IF EXISTS provinces;
-- DROP TABLE IF EXISTS administrative_units;
-- DROP TABLE IF EXISTS administrative_regions;

-- CREATE administrative_regions TABLE
CREATE TABLE administrative_regions (
                                        id integer NOT NULL,
                                        name varchar(255) NOT NULL,
                                        name_en varchar(255) NOT NULL,
                                        code_name varchar(255) NULL,
                                        code_name_en varchar(255) NULL,
                                        CONSTRAINT administrative_regions_pkey PRIMARY KEY (id)
);


-- CREATE administrative_units TABLE
CREATE TABLE administrative_units (
                                      id integer NOT NULL,
                                      full_name varchar(255) NULL,
                                      full_name_en varchar(255) NULL,
                                      short_name varchar(255) NULL,
                                      short_name_en varchar(255) NULL,
                                      code_name varchar(255) NULL,
                                      code_name_en varchar(255) NULL,
                                      CONSTRAINT administrative_units_pkey PRIMARY KEY (id)
);


-- CREATE provinces TABLE
CREATE TABLE provinces (
                           code varchar(20) NOT NULL,
                           name varchar(255) NOT NULL,
                           name_en varchar(255) NULL,
                           full_name varchar(255) NOT NULL,
                           full_name_en varchar(255) NULL,
                           code_name varchar(255) NULL,
                           administrative_unit_id integer NULL,
                           CONSTRAINT provinces_pkey PRIMARY KEY (code)
);


-- provinces foreign keys

ALTER TABLE provinces ADD CONSTRAINT provinces_administrative_unit_id_fkey FOREIGN KEY (administrative_unit_id) REFERENCES administrative_units(id);
CREATE INDEX idx_provinces_unit ON provinces(administrative_unit_id);

-- CREATE wards TABLE
CREATE TABLE wards (
                       code varchar(20) NOT NULL,
                       name varchar(255) NOT NULL,
                       name_en varchar(255) NULL,
                       full_name varchar(255) NULL,
                       full_name_en varchar(255) NULL,
                       code_name varchar(255) NULL,
                       province_code varchar(20) NULL,
                       administrative_unit_id integer NULL,
                       CONSTRAINT wards_pkey PRIMARY KEY (code)
);

-- wards foreign keys

ALTER TABLE wards ADD CONSTRAINT wards_administrative_unit_id_fkey FOREIGN KEY (administrative_unit_id) REFERENCES administrative_units(id);
ALTER TABLE wards ADD CONSTRAINT wards_province_code_fkey FOREIGN KEY (province_code) REFERENCES provinces(code);

CREATE INDEX idx_wards_province ON wards(province_code);
CREATE INDEX idx_wards_unit ON wards(administrative_unit_id);

-- 5. MERCHANT ADDRESSES

CREATE TABLE merchant_addresses (
                                    id VARCHAR(36) PRIMARY KEY,

                                    merchant_id VARCHAR(36) NOT NULL,
                                    merchant_address VARCHAR(255) NOT NULL,

    -- Địa chỉ hành chính mới
                                    province_code VARCHAR(20) NOT NULL,
                                    ward_code VARCHAR(20) NOT NULL,

                                    merchant_open_time TIME  NULL,
                                    merchant_close_time TIME  NULL,

                                    is_default BOOLEAN NOT NULL DEFAULT FALSE,

                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

                                    FOREIGN KEY (merchant_id)
                                        REFERENCES merchants(id),

                                    FOREIGN KEY (province_code)
                                        REFERENCES provinces(code),

                                    FOREIGN KEY (ward_code)
                                        REFERENCES wards(code)
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