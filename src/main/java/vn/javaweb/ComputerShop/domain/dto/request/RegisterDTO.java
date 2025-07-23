package vn.javaweb.ComputerShop.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.javaweb.ComputerShop.service.validator.RegisterChecked;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RegisterChecked
public class RegisterDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;

    public static class Builder {
        private String firstNameB;
        private String lastNameB;
        private String emailB;
        private String passwordB;
        private String confirmPasswordB;

        public Builder firstName (String n){
            this.firstNameB = n;
            return this;
        }

        public Builder lastName (String n){
            this.firstNameB = n;
            return this;
        }
        public Builder email (String n){
            this.emailB = n;
            return this;
        }
        public Builder password (String n){
            this.passwordB = n;
            return this;
        }
        public Builder confirmPassword (String n){
            this.confirmPasswordB = n;
            return this;
        }

        public RegisterDTO build (){
            RegisterDTO rs = new RegisterDTO();
            rs.firstName= firstNameB;
            rs.lastName = lastNameB;
            rs.email= emailB;
            rs.password = passwordB;
            rs.confirmPassword= confirmPasswordB;

            return rs;
        }

    };
}
