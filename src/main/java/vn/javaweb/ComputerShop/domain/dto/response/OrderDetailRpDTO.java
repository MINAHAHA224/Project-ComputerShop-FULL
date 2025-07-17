package vn.javaweb.ComputerShop.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderDetailRpDTO {


    private String productImage;
    private Long productId;
    private String productName;
    private Double price;
    private Long productQuantity;
}
