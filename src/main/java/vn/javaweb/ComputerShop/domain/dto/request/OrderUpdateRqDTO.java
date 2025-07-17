package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class OrderUpdateRqDTO {
    private Long id; // Không có validation
    private Double totalPrice; // Không có validation
    private String status; // Không có validation
    private String typePayment; // Không có validation

    @NotBlank(message = "{OrderUpdateRqDTO.receiverName.NotBlank}")
    @Size(max = 100, message = "{OrderUpdateRqDTO.receiverName.Size}")
    private String receiverName;

    @NotBlank(message = "{OrderUpdateRqDTO.receiverAddress.NotBlank}")
    @Size(max = 255, message = "{OrderUpdateRqDTO.receiverAddress.Size}")
    private String receiverAddress;

    @NotBlank(message = "{OrderUpdateRqDTO.receiverPhone.NotBlank}")
    @Pattern(regexp = "^(0\\d{9}|\\+84\\d{9,10})$", message = "{OrderUpdateRqDTO.receiverPhone.Pattern}")
    private String receiverPhone;

    private String statusPayment; // Không có validation

}
