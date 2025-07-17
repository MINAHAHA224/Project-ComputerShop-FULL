package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Validated
public class ProductUpdateRqDTO {
    private Long id; // Không có validation

    private String image; // Không có validation

    @NotBlank(message = "{ProductUpdateRqDTO.name.NotBlank}")
    @Size(max = 100, message = "{ProductUpdateRqDTO.name.Size}")
    private String name;

    @NotBlank(message = "{ProductUpdateRqDTO.factory.NotBlank}")
    @Size(max = 100, message = "{ProductUpdateRqDTO.factory.Size}")
    private String factory;

    @NotNull(message = "{ProductUpdateRqDTO.price.NotNull}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{ProductUpdateRqDTO.price.DecimalMin}")
    private Double price;

    @NotBlank(message = "{ProductUpdateRqDTO.detailDesc.NotBlank}")
    private String detailDesc;

    @NotBlank(message = "{ProductUpdateRqDTO.shortDesc.NotBlank}")
    @Size(max = 255, message = "{ProductUpdateRqDTO.shortDesc.Size}")
    private String shortDesc;

    @NotNull(message = "{ProductUpdateRqDTO.quantity.NotNull}")
    @Min(value = 1, message = "{ProductUpdateRqDTO.quantity.Min}")
    private Long quantity;

    @NotBlank(message = "{ProductUpdateRqDTO.target.NotBlank}")
    private String target;

    @NotNull(message = "{ProductUpdateRqDTO.sold.NotNull}")
    @Min(value = 0, message = "{ProductUpdateRqDTO.sold.Min}")
    private Long sold;

}
