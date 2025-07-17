package vn.javaweb.ComputerShop.domain.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
@Getter

public enum OrderStatus {
    PENDING("Chờ xác nhận"),     // Chờ xác nhận
    CONFIRMED("Đã xác nhận đơn"),   // Đã xác nhận đơn
    SHIPPED("Đã giao cho bên vận chuyển"),     // Đã giao cho bên vận chuyển
    DELIVERED("Giao hàng thành công"),   // Giao hàng thành công
    CANCELLED("Hủy đơn hàng");   // Hủy đơn hàng

    // Khách hoàn hàng

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public static Map<String, String> getOrderStatusMap() {
        Map<String, String> map = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            map.put(status.name(), status.description);
        }
        return map;
    }
}
