SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (
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
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        'admin@mealchoice.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO6jJ9kJ9Qz3nK8W4L5xT6u7V8w9X0Y1Z',
        'Admin',
        '0900000001',
        'MALE',
        NULL,
        '1995-01-01',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'user1@mealchoice.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO6jJ9kJ9Qz3nK8W4L5xT6u7V8w9X0Y1Z',
        'Nguyễn Văn An',
        '0900000002',
        'MALE',
        NULL,
        '2000-05-10',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'merchant1@mealchoice.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO6jJ9kJ9Qz3nK8W4L5xT6u7V8w9X0Y1Z',
        'Trần Minh Anh',
        '0900000003',
        'FEMALE',
        NULL,
        '1998-08-20',
        TRUE,
        CURRENT_TIMESTAMP
    );