package vn.javaweb.ComputerShop.service.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import vn.javaweb.ComputerShop.domain.dto.request.*;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.domain.dto.response.UserDetailDTO;
import vn.javaweb.ComputerShop.domain.dto.response.UserRpDTO;

import java.util.List;
import java.util.Locale;

 public interface UserService {

     ResponseBody handleLogin(LoginDTO loginDTO, HttpSession session, Locale locale);
     ResponseBody handleRegister(RegisterDTO registerDTO , Locale  locale);
     ModelAndView handleRedirectToGoogle();
     ResponseBody handleLoginOauth2Google(String code, Locale locale, HttpSession session);
     ResponseBody handleSendOTP(String email , Locale locale);
     ResponseBody handleVerifyOTP(String email, String OTP , Locale locale);
     ResponseBody handleResetPassword(ResetPasswordDTO resetPasswordDTO , Locale locale);
     List<UserRpDTO> handleGetUsers();
     ResponseBody handleCreateUser (UserCreateRqDTO userCreateRqDTO , MultipartFile file);
     UserDetailDTO handleGetUserDetail (Long id);
     UserUpdateRqDTO handleShowDataUserUpdate (Long id );
     ResponseBody handleUpdateUser (UserUpdateRqDTO userUpdateRqDTO , MultipartFile file);
     ResponseBody handleDeleteUser (Long id);
     UserProfileUpdateDTO handleGetDataUserToProfile (HttpSession session);
     ResponseBody handleUpdateProfile (HttpSession  session , UserProfileUpdateDTO userProfileUpdateDTO , Locale locale);
     ResponseBody handleUpdateAvatar (HttpSession session , MultipartFile avatarFile , Locale locale );
     ResponseBody handleUpdatePassword (HttpSession session , ChangePasswordDTO changePasswordDTO , Locale locale);
}