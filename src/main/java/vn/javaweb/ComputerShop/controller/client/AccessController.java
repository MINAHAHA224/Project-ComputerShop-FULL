package vn.javaweb.ComputerShop.controller.client;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.javaweb.ComputerShop.component.MessageComponent;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.request.LoginDTO;
import vn.javaweb.ComputerShop.domain.dto.request.RegisterDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ResetPasswordDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.service.product.ProductService;
import vn.javaweb.ComputerShop.service.user.UserService;
import java.util.Locale;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccessController {
    private final UserService userService;
    private final ProductService productService;
    private final MessageComponent messageComponent;

    @GetMapping("/login")
    public String getLogin(Model model , Locale locale) {
        LoginDTO loginDTO = new LoginDTO();
        model.addAttribute("loginDTO", loginDTO);
        return "client/auth/login";
    }

    @PostMapping("/login")
    public String postLogin(Model model, HttpSession session , Locale locale
            , @Valid @ModelAttribute("loginDTO") LoginDTO loginDTO
            , BindingResult bindingResult
            ,  RedirectAttributes redirectAttributes
           ) {


        List<FieldError> errors = bindingResult.getFieldErrors();
        if (bindingResult.hasErrors()) {
            for (FieldError error : errors) {
                System.out.println("--ER " + error.getField() + " - " + error.getDefaultMessage());
            }
            model.addAttribute("loginDTO", loginDTO);
            return "client/auth/login";
        }
        ResponseBody handleLogin = this.userService.handleLogin(loginDTO, session , locale);
        if (handleLogin.getStatus() == 200 && ((InformationDTO) handleLogin.getData()).getRole().equals("ADMIN")) {
            session.setAttribute("informationDTO", handleLogin.getData());
            model.addAttribute("messageSuccess" ,handleLogin.getMessage());
            return "redirect:/admin";
        } else if (handleLogin.getStatus() == 200) {
            session.setAttribute("informationDTO", handleLogin.getData());
            redirectAttributes.addFlashAttribute("messageSuccess" ,handleLogin.getMessage());
            return "redirect:/home";
        } else {
            redirectAttributes.addFlashAttribute("messageError", handleLogin.getMessage());
            return "redirect:/login";

        }
    }


    @GetMapping("/register")
    public String getRegister(Model model) {

        model.addAttribute("registerDTO", new RegisterDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String postRegister(Model model, @Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                               BindingResult bindingResult , Locale locale) {
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println("--ER " + error.getField() + " - " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            return "client/auth/register";
        }

        ResponseBody handleRegister = this.userService.handleRegister(registerDTO , locale);
        if (handleRegister.getStatus() == 200) {
            model.addAttribute("messageSuccess", handleRegister.getMessage());
            return "redirect:/login";
        } else {
            model.addAttribute("messageError", handleRegister.getMessage());
            return "client/auth/register";
        }
    }


    @GetMapping(value = "/auth/google")
    public ModelAndView redirectToGoogle() {
        return this.userService.handleRedirectToGoogle();
    }

    @GetMapping("/auth/oauth2/code/google")
    public String handleGoogleCallback(@RequestParam("code") String code , RedirectAttributes redirectAttributes
            , HttpSession session , Model model ,Locale locale) {

        ResponseBody result = this.userService.handleLoginOauth2Google(code, locale, session);
        if ( result.getStatus() == 200) {
            InformationDTO informationDTO = (InformationDTO) result.getData();
            session.setAttribute("informationDTO", informationDTO);
            redirectAttributes.addFlashAttribute("messageSuccess" ,result.getMessage() );
        }



        return "redirect:/home";
    }


    @GetMapping("/forgotPassword")
    public String getPageForgotPassword(Model model) {
        model.addAttribute("email", "");
        return "client/auth/forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String postForgotPassword(Model model
            , @RequestParam(name = "email", required = true) String email
            , RedirectAttributes redirectAttributes , Locale locale) {
        ResponseBody sendOTPtoEmail = this.userService.handleSendOTP(email.trim() , locale);
        if (sendOTPtoEmail.getStatus() != 200) {
            model.addAttribute("messageError", sendOTPtoEmail.getMessage());
            model.addAttribute("email", email.trim());
            return "client/auth/forgotPassword";
        } else {
            model.addAttribute("email", email.trim());
            model.addAttribute("messageSuccess", sendOTPtoEmail.getMessage());
            return "client/auth/verifyOTP";
        }

    }

//    @GetMapping("/verifyOTP")
//    public String getVerifyOTP(Model model,
//                               @ModelAttribute("email") String email // Nhận email từ flash attribute
//    ) {
//        if (email == null || email.isEmpty()) {
//            return "client/auth/verifyOTP";
//        }
//        model.addAttribute("email", email);
//        return "client/auth/verifyOTP";
//    }

    @PostMapping("/verifyOtp")
    public String postOTPtoVerify(Model model
            , @RequestParam(name = "email", required = true) String email
            , @RequestParam(name = "OTP", required = false) String OTP
            , @RequestParam(name = "action", required = true) String action
            , RedirectAttributes redirectAttributes , Locale locale) {
        if (action.equals("VERIFY-OTP")) {
            ResponseBody sendOTPtoEmail = this.userService.handleVerifyOTP(email, OTP , locale);
            if (sendOTPtoEmail.getStatus() != 200) {
                model.addAttribute("email", email);
                model.addAttribute("messageError", sendOTPtoEmail.getMessage());
                return "client/auth/verifyOTP";
            } else {
                ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
                resetPasswordDTO.setEmail(email);
                model.addAttribute("resetPasswordDTO", resetPasswordDTO);
                model.addAttribute("messageSuccess", sendOTPtoEmail.getMessage());
                return "client/auth/resetPassword";
            }
        }
        if (action.equals("RESENT-OTP")) {
            ResponseBody resentOtp = this.userService.handleSendOTP(email.trim() , locale);
            model.addAttribute("email", email.trim());
            model.addAttribute("messageSuccess", resentOtp.getMessage());
            return "client/auth/verifyOTP";
        }
        return null;
    }

//    @GetMapping("/resetPassword")
//    public String getPageResetPassword(Model model,
//                                       @ModelAttribute("email") String email, // Nhận email từ flash attribute
//                                       HttpServletRequest request) {
//        if (email == null || email.isEmpty()) {
//            return "client/auth/forgotPassword";
//        }
//
//        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
//        resetPasswordDTO.setEmail(email); // Gán email vào DTO để hiển thị trong trường ẩn
//        model.addAttribute("resetPasswordDTO", resetPasswordDTO);
//        return "client/auth/resetPassword";
//    }

    @PostMapping(value = "/resetPassword")
    public String postResetPassword(Model model , Locale locale
            , @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO
            , BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println("-- ER " + error.getField() + " -- " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/auth/resetPassword";
        }

        ResponseBody handleResetPass = this.userService.handleResetPassword(resetPasswordDTO , locale);
        if (handleResetPass.getStatus() == 200) {
            redirectAttributes.addFlashAttribute("messageSuccess", handleResetPass.getMessage());
            return "redirect:/login";
        } else {
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            model.addAttribute("messageError", handleResetPass.getMessage());
            return "client/auth/resetPassword";
        }

    }


}
