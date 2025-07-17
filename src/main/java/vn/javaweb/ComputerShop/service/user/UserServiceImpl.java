package vn.javaweb.ComputerShop.service.user;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import vn.javaweb.ComputerShop.component.GoogleOauth2;
import vn.javaweb.ComputerShop.component.MailerComponent;
import vn.javaweb.ComputerShop.component.MessageComponent;
import vn.javaweb.ComputerShop.domain.dto.request.*;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.domain.dto.response.UserDetailDTO;
import vn.javaweb.ComputerShop.domain.dto.response.UserRpDTO;
import vn.javaweb.ComputerShop.domain.entity.*;
import vn.javaweb.ComputerShop.domain.enums.CartStatus;
import vn.javaweb.ComputerShop.handleException.AuthException;
import vn.javaweb.ComputerShop.repository.auth.AuthMethodRepository;
import vn.javaweb.ComputerShop.repository.cart.CartRepository;
import vn.javaweb.ComputerShop.repository.user.RoleRepository;
import vn.javaweb.ComputerShop.repository.user.UserOtpRepository;
import vn.javaweb.ComputerShop.repository.user.UserRepository;
import vn.javaweb.ComputerShop.service.upload.UploadService;
import vn.javaweb.ComputerShop.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthMethodRepository authMethodRepository;
    private final UserOtpRepository userOtpRepository;
    private final MailerComponent mailerComponent;
    private final UploadService uploadService;
    private final MessageComponent messageComponent;


    private final RestTemplate restTemplate = new RestTemplate();
    private final GoogleOauth2 googleOauth2;

    @Override
    public ResponseBody handleLogin(LoginDTO loginDTO, HttpSession session, Locale locale) {

        String email = loginDTO.getEmail().trim();
        String password = loginDTO.getPassword().trim();
        UserEntity user = new UserEntity();
        Optional<UserEntity> emailOnDb = this.userRepository.findUserEntityByEmail(email);
        if (emailOnDb.isPresent()) {
            user = emailOnDb.get();
        } else {
            return new ResponseBody(500, messageComponent.getLocalizedMessage("user.login.error.emailNotRegistered", locale));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new ResponseBody(500, messageComponent.getLocalizedMessage("user.login.error.incorrectPassword", locale));
        }


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                email,
                password,
                user.getAuthorities()
        );
        Authentication authenticationResult = authenticationManager.authenticate(authenticationToken);
        //add data into SecurityContextHolder to view used to authorized
//             SecurityContextHolder.getContext().setAuthentication(authenticationResult);
        // set session boi vi neu sai security rieng ma ko sai qua form login
        // thi thang Spring security no se ko tu di check , ma minh phai set session cho no no moi check
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        SecurityContextHolder.setContext(context);

        // gắn vào HttpSession để Spring Security nhận diện
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);


        // Lấy thông tin người dùng đã xác thực
        String testEmailFromSecurity = SecurityUtils.getPrincipal(); // Đây là username (email)
        log.info("Logged in user (from SecurityUtils after setAuthentication): {}  ", testEmailFromSecurity);
        log.info("Authorities in SecurityContext: {} ", SecurityContextHolder.getContext().getAuthentication().getAuthorities());


        InformationDTO informationDTO = new InformationDTO();
        informationDTO.setId(user.getId());
        informationDTO.setEmail(user.getEmail());
        informationDTO.setRole(user.getRole().getName());
        informationDTO.setFullName(user.getFullName());
        informationDTO.setAvatar(user.getAvatar());
        Optional<CartEntity> cartCurrent = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString());
        informationDTO.setSum(cartCurrent.map(CartEntity::getSum).orElse(0));
        session.setAttribute("email", user.getEmail());

        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.login.success", locale), informationDTO);
    }

    @Override
    @Transactional
    public ResponseBody handleRegister(RegisterDTO registerDTO, Locale locale) {
        try {
            UserEntity user = new UserEntity();
            user.setFullName(registerDTO.getFirstName() + " " + registerDTO.getLastName());
            user.setEmail(registerDTO.getEmail());
            user.setPassword(registerDTO.getPassword());
            String hashPassword = this.passwordEncoder.encode(user.getPassword());
            user.setPassword(hashPassword);
            RoleEntity role = this.roleRepository.findRoleEntityByName("USER");
            user.setRole(role);

            this.userRepository.save(user);
            return new ResponseBody(200, messageComponent.getLocalizedMessage("user.register.success", locale));
        } catch (RuntimeException e) {
            log.error("--ER handleRegister {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public ModelAndView handleRedirectToGoogle() {
        String url = googleOauth2.getAuthUrl();
        return new ModelAndView("redirect:" + url);
    }

    @Override
    @Transactional
    public ResponseBody handleLoginOauth2Google(String code, Locale locale, HttpSession session) {
        InformationDTO informationDTO = new InformationDTO();
        // 1. Get access token
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(
                googleOauth2.buildTokenRequestBody(code),
                googleOauth2.getHeadersForToken()
        );

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                googleOauth2.getTokenEndpoint(), tokenRequest, Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        //Call userinfo endpoint , google need bearer not bearer for user use to connect api to software
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                googleOauth2.getUserInfoEndpoint(), HttpMethod.GET, entity, Map.class);

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String external_id = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        boolean existEmail = this.userRepository.existsByEmail(email);
        // if exists => create token for security
        if (existEmail) {
            UserEntity user = this.userRepository.findUserEntityByEmail(email).orElseThrow(() -> new AuthException("User not found"));
            informationDTO.setId(user.getId());
            informationDTO.setEmail(user.getEmail());
            informationDTO.setFullName(user.getFullName());
            informationDTO.setAvatar(user.getAvatar());
            informationDTO.setRole(user.getRole().getName());
            Optional<CartEntity> cartCurrent = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString());
            informationDTO.setSum(cartCurrent.map(CartEntity::getSum).orElse(0));
            session.setAttribute("email", user.getEmail());


            Authentication BybassAuthenticationForLoginGoogle = new UsernamePasswordAuthenticationToken(email, null, user.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(BybassAuthenticationForLoginGoogle);
            SecurityContextHolder.setContext(context);

            // gắn vào HttpSession để Spring Security nhận diện
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

/**
 * can not get principle for login by Google
 *             String testEmailFromSecurity = SecurityUtils.getPrincipal(); // Đây là username (email)
 *             System.out.println("Logged in user (from SecurityUtils after setAuthentication): " + testEmailFromSecurity);
 *             System.out.println("Authorities in SecurityContext: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
 */

        } else {
            try {
                // save user first
                UserEntity userNew = new UserEntity();
                userNew.setEmail(email);
                userNew.setFullName(name);
                RoleEntity role = this.roleRepository.findRoleEntityByName("USER");
                userNew.setRole(role);
                UserEntity userCurrent = this.userRepository.save(userNew);

                // save AuthMethod second
                AuthMethodEntity authMethod = new AuthMethodEntity();
                authMethod.setUser(userCurrent);
                authMethod.setLogin_type("GOOGLE");
                authMethod.setExternal_id(external_id);
                this.authMethodRepository.save(authMethod);

            } catch (RuntimeException e) {
                log.error("--ER handleLoginOauth2Google  {} ", e.getMessage());
                throw e;
            }


            UserEntity user = this.userRepository.findUserEntityByEmail(email).orElseThrow(
                    () -> new AuthException("User not found")
            );
            informationDTO.setId(user.getId());
            informationDTO.setEmail(email);
            informationDTO.setFullName(name);
            informationDTO.setAvatar(picture);
            informationDTO.setRole(user.getRole().getName());
            informationDTO.setSum(0);

            session.setAttribute("email", email);
        }
        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.register.success", locale), informationDTO);
    }

    @Override
    @Transactional
    public ResponseBody handleSendOTP(String email, Locale locale) {

        Optional<UserEntity> user = this.userRepository.findUserEntityByEmail(email);
        if (user.isPresent()) {
            // second check email has OTP not yet expired , if it has , can not canSentEmail = false
            boolean canSentEmail = true;
            List<UserOtpEntity> listUserOtp = this.userOtpRepository.findUserOtpEntityByUser(user.get());
            for (UserOtpEntity userOtp : listUserOtp) {
                if (userOtp.getExpiredTime().isAfter(LocalDateTime.now())) {
                    canSentEmail = false;
                    break;
                }
            }
            if (canSentEmail) {
                try {

                    String OTP = this.mailerComponent.generateOTP(6);
                    boolean handleSend = this.mailerComponent.sendConfirmLink(email, OTP);
                    UserOtpEntity userOtp = new UserOtpEntity();
                    userOtp.setUser(user.get());
                    userOtp.setOtpCode(OTP);
                    userOtp.setCreatedAt(LocalDateTime.now());
                    userOtp.setExpiredTime(LocalDateTime.now().plusMinutes(1));
                    userOtp.setUsed(false);
                    // set OTP to database
                    this.userOtpRepository.save(userOtp);
                    // set body
                    return new ResponseBody(200, messageComponent.getLocalizedMessage("user.otp.success.sentToEmail", locale), OTP);
                } catch (RuntimeException e) {
                    log.error("--ER handleSendOTP  {}", e.getMessage());
                    throw e;
                }
            } else {
                return new ResponseBody(500, messageComponent.getLocalizedMessage("user.otp.error.alreadySentActive", locale));
            }
        } else {
            return new ResponseBody(500, messageComponent.getLocalizedMessage("user.otp.error.emailNotRegisteredOrError", locale));
        }
    }

    @Override
    @Transactional
    public ResponseBody handleVerifyOTP(String email, String OTP, Locale locale) {
        Optional<UserEntity> user = this.userRepository.findUserEntityByEmail(email);
        if (user.isPresent()) {
            // check first email have OTP not yet Expired if has userOtpEnough = have data  otherwise has no data
            List<UserOtpEntity> listUserOtp = this.userOtpRepository.findUserOtpEntityByUser(user.get());
            UserOtpEntity userOtpEnough = new UserOtpEntity();
            for (UserOtpEntity userOtp : listUserOtp) {
                if (userOtp.getExpiredTime().isAfter(LocalDateTime.now())) {
                    userOtpEnough = userOtp;
                    break;
                }
            }

            // if has userOtpEnough = have data , accept and update userOtp
            if (userOtpEnough.getId() != null) {
                boolean used = userOtpEnough.isUsed();
                String otpDb = userOtpEnough.getOtpCode();

                if (otpDb.equals(OTP) && !used) {
                    // Update OTP is used
                    try {
                        userOtpEnough.setUsed(true);
                        this.userOtpRepository.save(userOtpEnough);
                        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.otp.success.verified", locale), OTP);
                    } catch (RuntimeException e) {
                        log.error("-- ER update userOtp  {}", e.getMessage());
                        throw e;
                    }

                } else {
                    return new ResponseBody(500, messageComponent.getLocalizedMessage("user.otp.error.invalidOrExpiredOrUsed", locale), OTP);
                }
                //  otherwise has no data , announcement error
            } else {
                return new ResponseBody(500, messageComponent.getLocalizedMessage("user.otp.error.invalidOrExpiredOrUsed", locale));
            }
        } else {

            return new ResponseBody(500, messageComponent.getLocalizedMessage("user.otp.error.emailNotFound", locale));
        }
    }

    @Override
    @Transactional
    public ResponseBody handleResetPassword(ResetPasswordDTO resetPasswordDTO, Locale locale) {

        try {
            UserEntity user = this.userRepository.findUserEntityByEmail(resetPasswordDTO.getEmail().trim()).orElseThrow(() -> new AuthException("User not found"));
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));
            this.userRepository.save(user);
            return new ResponseBody(200, messageComponent.getLocalizedMessage("user.resetPassword.success", locale));
        } catch (RuntimeException e) {
            log.error("--ER handleResetPassword {} ", e.getMessage());
            throw e;
        }


    }

    @Override
    public List<UserRpDTO> handleGetUsers() {
        List<UserEntity> listEntity = this.userRepository.findAll();
        return listEntity.stream().map(us ->
                UserRpDTO.builder()
                        .id(us.getId())
                        .email(us.getEmail())
                        .fullName(us.getFullName())
                        .nameRole(us.getRole().getName())
                        .build()
        ).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ResponseBody handleCreateUser(UserCreateRqDTO userCreateRqDTO, MultipartFile file) {

        String email = userCreateRqDTO.getEmail().trim();
        String address = userCreateRqDTO.getAddress().trim();
        String phone = userCreateRqDTO.getPhone().trim();
        String fullName = userCreateRqDTO.getFullName().trim();
        String avatar = this.uploadService.handleUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(userCreateRqDTO.getPassword());
        RoleEntity role = this.roleRepository.findRoleEntityByName(userCreateRqDTO.getRoleName());
        // handle check email and password
        boolean checkEmailExist = this.userRepository.existsUserEntityByEmail(email);
        if (checkEmailExist) {
            return new ResponseBody(500, "Admin : email đã có tài khoản sử dụng");
        }
        boolean checkExistPhone = this.userRepository.existsUserEntityByPhone(phone);
        if (checkExistPhone) {
            return new ResponseBody(500, "Admin : Số tài khoản đã được sử dụng");
        }
        // save user
        UserEntity user = UserEntity.builder()
                .email(email)
                .address(address)
                .phone(phone)
                .fullName(fullName)
                .avatar(avatar)
                .password(hashPassword)
                .role(role)
                .build();
        this.userRepository.save(user);
        return new ResponseBody(200, "Admin : tạo tài khoản thành công");

    }

    @Override
    public UserDetailDTO handleGetUserDetail(Long id) {
        UserEntity user = this.userRepository.findUserEntityById(id);
        if (user == null) {
            throw new AuthException("User not found");
        }
        return UserDetailDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roleName(user.getRole().getName())
                .avatar(user.getAvatar())
                .build();
    }


    @Override
    public UserUpdateRqDTO handleShowDataUserUpdate(Long id) {
        UserEntity user = this.userRepository.findUserEntityById(id);
        if (user == null) {
            throw new AuthException("User not found");
        }
        return UserUpdateRqDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roleName(user.getRole().getName())
                .avatar(user.getAvatar())
                .build();
    }


    @Override
    @Transactional
    public ResponseBody handleUpdateUser(UserUpdateRqDTO userUpdateRqDTO, MultipartFile file) {
        UserEntity userCurrent = this.userRepository.findUserEntityById(userUpdateRqDTO.getId());
        if (userCurrent == null) {
            throw new AuthException("User not found");
        }

        RoleEntity role = this.roleRepository.findRoleEntityByName(userUpdateRqDTO.getRoleName().trim());
        String phone = userUpdateRqDTO.getPhone().trim();

        // handle check phone
        boolean checkExistPhone = this.userRepository.existsUserEntityByPhone(phone);
        if (checkExistPhone) {
            return new ResponseBody(500, "Admin : Số điện thoại đã được sử dụng");
        }
        // set data new
        userCurrent.setFullName(userUpdateRqDTO.getFullName());
        userCurrent.setAddress(userUpdateRqDTO.getAddress());
        userCurrent.setPhone(userUpdateRqDTO.getPhone());
        userCurrent.setRole(role);
        if (file != null && !Objects.equals(file.getOriginalFilename(), "")) {
            String newAvatar = this.uploadService.handleUploadFile(file, "avatar");
            userCurrent.setAvatar(newAvatar);
        }
        this.userRepository.save(userCurrent);
        return new ResponseBody(200, "Admin : Cập nhật tài khoản người dùng thành công");
    }


    @Override
    @Transactional
    public ResponseBody handleDeleteUser(Long id) {
        UserEntity user = this.userRepository.findUserEntityById(id);
        if (user != null) {
            throw new AuthException("User not found");
        }
        this.userRepository.deleteUserEntityById(id);
        return new ResponseBody(200, "Admin : Xóa tài khoản thành công");
    }

    @Override
    public UserProfileUpdateDTO handleGetDataUserToProfile(HttpSession session) {
        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);
        UserEntity userEntity = this.userRepository.findUserEntityByEmail(informationDTO.getEmail()).orElseThrow(
                () -> new AuthException("User not found")
        );
        boolean checkOauth2 = this.authMethodRepository.existsAuthMethodEntityByUser(userEntity);

        return UserProfileUpdateDTO.builder()
                .email(userEntity.getEmail())
                .fullName(userEntity.getFullName())
                .address(userEntity.getAddress())
                .avatar(userEntity.getAvatar())
                .phone(userEntity.getPhone())
                .hasChangePass(!checkOauth2)
                .build();
    }


    @Override
    @Transactional
    public ResponseBody handleUpdateProfile(HttpSession session, UserProfileUpdateDTO userProfileUpdateDTO, Locale locale) {
        UserEntity user = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session)).orElseThrow(
                () -> new AuthException("User not found")
        );
        boolean checkPhone = this.userRepository.existsUserEntityByPhone(userProfileUpdateDTO.getPhone().trim());

        if (user.getPhone() == null || !user.getPhone().equals(userProfileUpdateDTO.getPhone())) {
            if (checkPhone) {
                return new ResponseBody(500, messageComponent.getLocalizedMessage("user.profile.update.error.phoneExists", locale));
            }
            user.setPhone(userProfileUpdateDTO.getPhone().trim());
        }

        user.setAddress(userProfileUpdateDTO.getAddress().trim());
        this.userRepository.save(user);
        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.profile.update.success", locale));
    }


    @Override
    @Transactional
    public ResponseBody handleUpdateAvatar(HttpSession session, MultipartFile avatarFile, Locale locale) {
        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);
        UserEntity user = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session)).orElseThrow(
                () -> new AuthException("User not found")
        );

        if (Objects.equals(avatarFile.getOriginalFilename(), "") || avatarFile.isEmpty()) {
            return new ResponseBody(500, messageComponent.getLocalizedMessage("avatar.update.error.emptyFile", locale));
        }

        String avatarNew = this.uploadService.handleUploadFile(avatarFile, "profile");
        user.setAvatar(avatarNew);
        this.userRepository.save(user);
        // set session avatar
        informationDTO.setAvatar(avatarNew);
        session.setAttribute("informationDTO", informationDTO);

        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.avatar.update.success", locale));
    }


    @Override
    @Transactional
    public ResponseBody handleUpdatePassword(HttpSession session, ChangePasswordDTO changePasswordDTO, Locale locale) {
        UserEntity user = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session)).orElseThrow(
                () -> new AuthException("User not found")
        );
        boolean checkPass = this.passwordEncoder.matches(changePasswordDTO.getCurrentPassword().trim(), user.getPassword());
        if (!checkPass) {
            return new ResponseBody(500, messageComponent.getLocalizedMessage("user.password.update.error.newPasswordMismatch", locale));
        }
        user.setPassword(this.passwordEncoder.encode(changePasswordDTO.getNewPassword().trim()));
        this.userRepository.save(user);
        return new ResponseBody(200, messageComponent.getLocalizedMessage("user.password.update.success", locale));
    }


}
