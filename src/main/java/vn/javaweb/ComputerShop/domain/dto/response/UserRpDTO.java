package vn.javaweb.ComputerShop.domain.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserRpDTO {
    private Long id;
    private String email;
    private String fullName;
    private String nameRole;
}
