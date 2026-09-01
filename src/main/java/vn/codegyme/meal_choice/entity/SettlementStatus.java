package vn.codegyme.meal_choice.entity;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    PENDING_CONFIRMATION("Chờ xác nhận", "bg-warning text-dark"),
    CONFIRMED("Đã xác nhận", "bg-success text-white"),
    DISPUTED("Đang khiếu nại", "bg-danger text-white");

    private final String displayName;
    private final String badgeClass;

    SettlementStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }
}
