package vn.javaweb.ComputerShop.domain.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
@Getter
public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),      // Chưa thanh toán
    PAID("Đã thanh toán"),        // Đã thanh toán
    REFUNDED("Đã hoàn tiền"),    // Đã hoàn tiền
    FAILED("Thanh toán thất bại")  ;     // Thanh toán thất bại

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public static Map<String, String> getPaymentStatusMap() {
        Map<String, String> map = new HashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            map.put(status.name(), status.description);
        }
        return map;
    }

}
