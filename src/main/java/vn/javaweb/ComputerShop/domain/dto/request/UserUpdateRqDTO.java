package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserUpdateRqDTO {

    private Long id; // Không có validation

    private String email; // Không có validation

    @NotBlank(message = "{UserUpdateRqDTO.phone.NotBlank}")
    @Pattern(regexp = "^\\d{10,11}$", message = "{UserUpdateRqDTO.phone.Pattern}")
    private String phone;

    @NotBlank(message = "{UserUpdateRqDTO.fullName.NotBlank}")
    @Size(max = 100, message = "{UserUpdateRqDTO.fullName.Size}")
    private String fullName;

    @NotBlank(message = "{UserUpdateRqDTO.address.NotBlank}")
    @Size(max = 255, message = "{UserUpdateRqDTO.address.Size}")
    private String address;

    @NotBlank(message = "{UserUpdateRqDTO.roleName.NotBlank}")
    private String roleName;

    private String avatar; // Không có validation

}
