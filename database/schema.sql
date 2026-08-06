DROP DATABASE IF EXISTS meal_choice;
CREATE DATABASE meal_choice;
SELECT COUNT(*) FROM meal_choice.merchants;
INSERT INTO meal_choice.merchants
(
    id,
    restaurant_name,
    merchant_email,
    merchant_phone,
    merchant_address,
    merchant_open_time,
    merchant_close_time,
    merchant_status,
    user_id
)
VALUES
    (
        UUID_TO_BIN(UUID()),
        'Quán Cơm Gà Nam',
        'nam@merchant.com',
        '0900000004',
        'Hà Nội',
        '08:00:00',
        '22:00:00',
        'APPROVED',
        NULL
    ),
    (
        UUID_TO_BIN(UUID()),
        'Bếp Nhà Hoa',
        'hoa@merchant.com',
        '0900000005',
        'TP Hồ Chí Minh',
        '09:00:00',
        '21:30:00',
        'PENDING',
        NULL
    ),
    (
        UUID_TO_BIN(UUID()),
        'Pizza Ý Ngon',
        'minh@merchant.com',
        '0900000006',
        'Đà Nẵng',
        '10:00:00',
        '23:00:00',
        'APPROVED',
        NULL
    ),
    (
        UUID_TO_BIN(UUID()),
        'Trà Sữa Mộc',
        'lan@merchant.com',
        '0900000007',
        'Hải Phòng',
        '08:30:00',
        '22:30:00',
        'REJECTED',
        NULL
    ),
    (
        UUID_TO_BIN(UUID()),
        'Bún Chả Hà Thành',
        'duc@merchant.com',
        '0900000008',
        'Hà Nội',
        '07:00:00',
        '20:00:00',
        'BLOCKED',
        NULL
    );