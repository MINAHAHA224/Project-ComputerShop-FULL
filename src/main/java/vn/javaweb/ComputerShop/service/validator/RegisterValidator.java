package vn.javaweb.ComputerShop.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import vn.javaweb.ComputerShop.domain.dto.request.RegisterDTO;
import vn.javaweb.ComputerShop.repository.user.UserRepository;


import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegisterValidator implements ConstraintValidator<RegisterChecked, RegisterDTO> {

    private final UserRepository userRepository;
    private static final String EMAIL_REGEX = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public boolean isValid(RegisterDTO registerDTO, ConstraintValidatorContext context) {

        if (!StringUtils.hasText(registerDTO.getFirstName())) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.firstName.NotBlank}")
                    .addPropertyNode("firstName")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        if (!StringUtils.hasText(registerDTO.getLastName())) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.lastName.NotBlank}")
                    .addPropertyNode("lastName")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        if (!StringUtils.hasText(registerDTO.getEmail())) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.email.NotBlank}")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }




        if (!Pattern.matches(EMAIL_REGEX, registerDTO.getEmail())) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.email.Pattern}")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        try {
            if (this.userRepository.existsUserEntityByEmail(registerDTO.getEmail())) {
                context.buildConstraintViolationWithTemplate("{RegisterDTO.email.Exists}")
                        .addPropertyNode("email")
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.email.CheckError}")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        if (registerDTO.getPassword().length() < 6 || registerDTO.getPassword().length() > 20 ||
                registerDTO.getConfirmPassword().length() < 6 || registerDTO.getConfirmPassword().length() > 20) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.password.Length}")
                    .addPropertyNode("password")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            context.buildConstraintViolationWithTemplate("{RegisterDTO.password.Mismatch}")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }

        return true;
    }
}
