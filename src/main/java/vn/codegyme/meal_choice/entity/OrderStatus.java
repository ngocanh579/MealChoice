package vn.codegyme.meal_choice.entity;

public enum OrderStatus {
    PENDING,      // Chờ quán xác nhận
    CONFIRMED,    // Quán đã xác nhận
    PREPARING,    // Đang chuẩn bị món
    DELIVERING,   // Đang giao hàng
    COMPLETED,    // Đã giao thành công
    CANCELLED     // Đã hủy
}
