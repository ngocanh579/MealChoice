USE meal_choice;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE foods;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO foods (
    id,
    merchant_id,
    merchant_address_id,
    food_category_id,
    food_name,
    preparation_time,
    food_note,
    price,
    discount_price,
    service_fee,
    views,
    order_count,
    is_recommended,
    created_at,
    updated_at,
    deleted_at
)
VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000002',
        'Phở Bò Đặc Biệt',
        15,
        'Phở bò truyền thống.',
        55000.00,
        49000.00,
        0.00,
        120,
        35,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        'Bánh Mì Trứng',
        10,
        'Bánh mì nóng giòn.',
        30000.00,
        25000.00,
        0.00,
        85,
        20,
        FALSE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        '50000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000003',
        '30000000-0000-0000-0000-000000000003',
        '40000000-0000-0000-0000-000000000003',
        'Cơm Gà Sốt Tiêu',
        20,
        'Cơm gà kèm sốt tiêu đặc biệt.',
        65000.00,
        NULL,
        0.00,
        64,
        12,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    );