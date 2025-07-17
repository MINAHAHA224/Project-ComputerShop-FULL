package vn.javaweb.ComputerShop.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CartDetailRpDTO {
    private Long productId;
    private String productImage;
    private String productName;

    // cartDetail
    private Long id;
    private Long quantity;
    private Double price;
    // total price

    private Long stockQuantity;

}
