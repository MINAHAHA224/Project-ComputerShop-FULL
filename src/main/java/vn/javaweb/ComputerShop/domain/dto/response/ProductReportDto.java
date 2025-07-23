package vn.javaweb.ComputerShop.domain.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductReportDto {
    private Long id;
    private String name;
    private Double price;
    private String shortDesc; // Tương ứng với short_desc trong DB
    private Long quantity;
    private Long sold;
}
