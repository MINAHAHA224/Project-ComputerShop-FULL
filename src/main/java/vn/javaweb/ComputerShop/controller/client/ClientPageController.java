package vn.javaweb.ComputerShop.controller.client;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.javaweb.ComputerShop.domain.dto.request.ChangePasswordDTO;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductFilterDTO;
import vn.javaweb.ComputerShop.domain.dto.request.UserProfileUpdateDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductFilterRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.service.product.ProductService;
import vn.javaweb.ComputerShop.service.upload.UploadService;
import vn.javaweb.ComputerShop.service.user.UserService;

@Controller
@RequiredArgsConstructor
public class ClientPageController {
    private final ProductService productService;
    private final UserService userService;
    private final UploadService uploadService;


    @GetMapping("/home")
    public String getHomepage(Model model , HttpSession session) {
        List<ProductRpDTO> listResult = this.productService.getAllProductView();
        model.addAttribute("products", listResult);


        // mang product danh de search

        List< Map<String , String >> dataSearch = new ArrayList<>();

        for (ProductRpDTO result : listResult) {
            Map<String, String> productInfo = new HashMap<>();
            productInfo.put("id", result.getId().toString());
            productInfo.put("name", result.getName());
            productInfo.put("image", result.getImage());
            productInfo.put("price", result.getPrice().toString());
            productInfo.put("formattedPrice", String.format("%,.0f đ", result.getPrice()));
            dataSearch.add( productInfo);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String dataSearchJson = objectMapper.writeValueAsString(dataSearch);
            model.addAttribute("dataSearchJson", dataSearchJson);
        } catch (Exception e) {
            model.addAttribute("dataSearchJson", "[]");
            e.printStackTrace();
        }


        return "client/homepage/show";
    }


    @GetMapping(value = "/")
    public String getHome() {
        return "redirect:/home";
    }


    @GetMapping(value = "/thanks")
    public String getThankYouPage (Model model){

        return "client/cart/thanks";
    }

    @GetMapping("/accessDeny")
    public String getAccessDenyPage() {
        return "client/auth/deny";
    }

    @GetMapping("/products")
    public String getProductsPage(Model model, ProductFilterDTO productFilterDTO, HttpServletRequest request) {

        ProductFilterRpDTO result = this.productService.handleShowDataProductFilter(productFilterDTO);

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + result.getPage(), "");
        }
        model.addAttribute("queryString", qs);

        model.addAttribute("products", result.getListProduct());
        model.addAttribute("currentPage", result.getPage());
        model.addAttribute("totalPages", result.getTotalPage());

        return "client/product/show";
    }


    @GetMapping(value = "/account-management")
    public String getAccountPage(Model model, HttpSession session) {
        UserProfileUpdateDTO userProfileUpdateDTO = this.userService.handleGetDataUserToProfile(session);
        model.addAttribute("userProfileUpdateDTO", userProfileUpdateDTO);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "client/profile/index";
    }

    @PostMapping(value = "/user/profile/update-info")
    public String postUpdateProfile (Model model ,
                                     RedirectAttributes redirectAttributes ,
                                     @Valid @ModelAttribute("userProfileUpdateDTO") UserProfileUpdateDTO userProfileUpdateDTO ,
                                     BindingResult bindingResult , HttpSession session , Locale locale){



            if (bindingResult.hasErrors() ){
                List<FieldError> errors = bindingResult.getFieldErrors();
                for (FieldError error : errors ){
                    System.out.println("-- ER  " + error.getField() + " - " + error.getDefaultMessage());
                }

                InformationDTO informationDTO = (InformationDTO) session.getAttribute("informationDTO") ;
                userProfileUpdateDTO.setAvatar(informationDTO.getAvatar());
                model.addAttribute("userProfileUpdateDTO", userProfileUpdateDTO);
                model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
                return "client/profile/index";
            }

        ResponseBody updateProfile = this.userService.handleUpdateProfile (session , userProfileUpdateDTO,locale);

        if (updateProfile.getStatus() != 200 ){
            model.addAttribute("userProfileUpdateDTO", userProfileUpdateDTO);
            model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
            model.addAttribute("messageError" , updateProfile.getMessage());
            return "client/profile/index";
        }else {

            redirectAttributes.addFlashAttribute("messageSuccess" ,updateProfile.getMessage());
            return "redirect:/account-management";
        }

    }


    @PostMapping(value = "/user/profile/change-password")
    public String postUpdateChangePass (Model model , RedirectAttributes redirectAttributes,
                                        @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO changePasswordDTO ,
                                        BindingResult bindingResult , HttpSession session , Locale locale){
        InformationDTO informationDTO = (InformationDTO) session.getAttribute("informationDTO") ;

        if ( bindingResult.hasErrors()){

            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors ){
                System.out.println("-- ER  " + error.getField() + " - " + error.getDefaultMessage());
            }
            UserProfileUpdateDTO userProfileUpdateDTO = this.userService.handleGetDataUserToProfile(session);

            userProfileUpdateDTO.setAvatar(informationDTO.getAvatar());
            userProfileUpdateDTO.setAvatar(informationDTO.getAvatar());
            model.addAttribute("userProfileUpdateDTO", userProfileUpdateDTO);
            model.addAttribute("changePasswordDTO", changePasswordDTO);

            return "client/profile/index";
        }

        if (!changePasswordDTO.getNewPassword().trim().equals(changePasswordDTO.getConfirmNewPassword().trim()) ){
            UserProfileUpdateDTO userProfileUpdateDTO = this.userService.handleGetDataUserToProfile(session);
            userProfileUpdateDTO.setAvatar(informationDTO.getAvatar());
            model.addAttribute("userProfileUpdateDTO", userProfileUpdateDTO);
            model.addAttribute("changePasswordDTO", changePasswordDTO);
            model.addAttribute("messageError" , "Mật khẩu nhập lai không khớp");
            return "client/profile/index";
        }

        ResponseBody updatePassword  = this.userService.handleUpdatePassword (session , changePasswordDTO , locale);
        if (updatePassword.getStatus() != 200 ){
            redirectAttributes.addFlashAttribute("messageError" , updatePassword.getMessage());
            return "redirect:/account-management";

        }else {
            redirectAttributes.addFlashAttribute("messageSuccess" , updatePassword.getMessage());
            return "redirect:/account-management";
        }


    }

    @PostMapping(value = "/user/profile/update-avatar")
    public String postUpdateAvatar (Model model , Locale locale,
                                   HttpSession session , RedirectAttributes redirectAttributes,
                                   @RequestParam("avatarFile") MultipartFile avatarFile){

       ResponseBody updateAvatar = this.userService.handleUpdateAvatar ( session , avatarFile , locale);

       if (updateAvatar.getStatus() != 200 ){
           redirectAttributes.addFlashAttribute("messageError" , updateAvatar.getMessage());
           return "redirect:/account-management";

       }else {
           redirectAttributes.addFlashAttribute("messageSuccess" , updateAvatar.getMessage());
           return "redirect:/account-management";
       }
    }



    @GetMapping(value = "/contact-us")
    public String getContactPage ( Model model) {
        return "client/contact/show";
    }

    @GetMapping(value = "/about-us")
    public String getAboutUsPage ( Model model) {
        return "client/about/show";
    }
}
