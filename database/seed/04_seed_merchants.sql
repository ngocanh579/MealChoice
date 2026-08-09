SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE merchants;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO merchants (
    id,
    user_id,
    merchant_restaurant_name,
    merchant_email,
    merchant_phone,
    merchant_status,
    is_trusted_partner
)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003',
        'Quán Ngon Hà Nội',
        'merchant1@mealchoice.com',
        '0900000003',
        'APPROVED',
        TRUE
    ),
    (
        '20000000-0000-0000-0000-000000000002',
        NULL,
        'Bếp Nhà An',
        'bepnha.an@mealchoice.com',
        '0900000004',
        'PENDING',
        FALSE
    ),
    (
        '20000000-0000-0000-0000-000000000003',
        NULL,
        'Ăn Vặt Góc Phố',
        'anvat.gocpho@mealchoice.com',
        '0900000005',
        'APPROVED',
        FALSE
    );