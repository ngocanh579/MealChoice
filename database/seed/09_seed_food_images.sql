USE meal_choice;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE food_images;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO food_images (
    id,
    food_id,
    image_url,
    is_primary
)
VALUES
    (
        '60000000-0000-0000-0000-000000000001',
        '50000000-0000-0000-0000-000000000001',
        '/uploads/foods/pho-bo-dac-biet.jpg',
        TRUE
    ),
    (
        '60000000-0000-0000-0000-000000000002',
        '50000000-0000-0000-0000-000000000002',
        '/uploads/foods/banh-mi-trung.jpg',
        TRUE
    ),
    (
        '60000000-0000-0000-0000-000000000003',
        '50000000-0000-0000-0000-000000000003',
        '/uploads/foods/com-ga-sot-tieu.jpg',
        TRUE
    );