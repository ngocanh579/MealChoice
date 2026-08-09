USE meal_choice;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE food_categories;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO food_categories (
    id,
    category_name,
    category_description
)
VALUES
    (
        '40000000-0000-0000-0000-000000000001',
        'Breakfast',
        'Món ăn phục vụ buổi sáng'
    ),
    (
        '40000000-0000-0000-0000-000000000002',
        'Lunch',
        'Món ăn phục vụ buổi trưa'
    ),
    (
        '40000000-0000-0000-0000-000000000003',
        'Dinner',
        'Món ăn phục vụ buổi tối'
    );