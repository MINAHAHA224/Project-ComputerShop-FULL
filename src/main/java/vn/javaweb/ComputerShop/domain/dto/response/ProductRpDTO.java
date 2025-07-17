package vn.javaweb.ComputerShop.domain.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductRpDTO implements Serializable {
    private Long id;
    private String name;      // Thêm dòng này
    private String image;
    private String shortDesc;
    private Double price;

}
