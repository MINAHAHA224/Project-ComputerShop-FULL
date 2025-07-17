package vn.javaweb.ComputerShop.domain.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
@Getter
public enum CartStatus {
    ACTIVE("Người dùng đang thêm sản phẩm"),      // Người dùng đang thêm sản phẩm
    ORDERED("Đã chuyển sang đơn hàng"),     // Đã chuyển sang đơn hàng
    ABANDONED("Bỏ giỏ hàng");

    private final String description;

    CartStatus(String description) {
        this.description = description;
    }

    public static Map<String, String> getCartStatusMap() {
        Map<String, String> map = new HashMap<>();
        for (CartStatus status : CartStatus.values()) {
            map.put(status.name(), status.description);
        }
        return map;
    }
}
