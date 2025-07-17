package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Validated
public class InfoOrderRqDTO {
    @NotBlank(message = "{InfoOrderRqDTO.receiverName.NotBlank}")
    @Size(max = 100, message = "{InfoOrderRqDTO.receiverName.Size}")
    private String receiverName;

    @NotBlank(message = "{InfoOrderRqDTO.receiverAddress.NotBlank}")
    @Size(max = 255, message = "{InfoOrderRqDTO.receiverAddress.Size}")
    private String receiverAddress;

    @NotBlank(message = "{InfoOrderRqDTO.receiverPhone.NotBlank}")
    @Pattern(regexp = "^(0\\d{9}|\\+84\\d{9,10})$", message = "{InfoOrderRqDTO.receiverPhone.Pattern}")
    private String receiverPhone;

    private Double totalPriceToSaveOrder; // Không có validation

    @NotEmpty(message = "{InfoOrderRqDTO.paymentMethod.NotEmpty}")
    private String paymentMethod;
}
