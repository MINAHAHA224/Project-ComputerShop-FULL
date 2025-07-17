package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.Email;
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
public class UserProfileUpdateDTO {

    @NotBlank(message = "{UserProfileUpdateDTO.email.NotBlank}")
    @Email(message = "{UserProfileUpdateDTO.email.Email}")
    private String email;

    @NotBlank(message = "{UserProfileUpdateDTO.fullName.NotBlank}")
    @Size(min = 2, max = 50, message = "{UserProfileUpdateDTO.fullName.Size}")
    private String fullName;

    @NotBlank(message = "{UserProfileUpdateDTO.phone.NotBlank}")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "{UserProfileUpdateDTO.phone.Pattern}")
    private String phone;

    @NotBlank(message = "{UserProfileUpdateDTO.address.NotBlank}")
    @Size(max = 100, message = "{UserProfileUpdateDTO.address.Size}")
    private String address;

    private String avatar; // Không có validation, không cần message key

    private boolean hasChangePass; // Không có validation, không cần message key
}
