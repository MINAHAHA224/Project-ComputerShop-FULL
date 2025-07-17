package vn.javaweb.ComputerShop.controller.admin;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.javaweb.ComputerShop.domain.dto.request.UserCreateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.UserUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.domain.dto.response.UserDetailDTO;
import vn.javaweb.ComputerShop.domain.dto.response.UserRpDTO;
import vn.javaweb.ComputerShop.service.user.UserService;


@Controller
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    @GetMapping("/admin/user")
    public String showUserPage(Model model) {
        List<UserRpDTO> listUser = this.userService.handleGetUsers();
        model.addAttribute("listUser", listUser);
        return "admin/user/show";
    }


    @GetMapping("/admin/user/create")
    public String getCreateUser(Model model) {
        model.addAttribute("userCreateRqDTO",new UserCreateRqDTO());
        return "admin/user/create";
    }

    @PostMapping("/admin/user/create")
    public String postCreateUser(Model model,
                                 @Valid @ModelAttribute("userCreateRqDTO") UserCreateRqDTO userCreateRqDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("avatarFile") MultipartFile file) {

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userCreateRqDTO" , userCreateRqDTO);
            return "admin/user/create";
        }
        ResponseBody result = this.userService.handleCreateUser(userCreateRqDTO , file);
        if (result.getStatus() == 200 ){
            model.addAttribute("messageSuccess" , result.getMessage());
            model.addAttribute("userCreateRqDTO" , new UserCreateRqDTO());
            return "admin/user/create";
        }else {
            model.addAttribute("messageError" , result.getMessage());
            model.addAttribute("userCreateRqDTO" , userCreateRqDTO);
            return "admin/user/create";
        }

    }


    @GetMapping("/admin/user/{id}")
    public String getDetailPage(Model model, @PathVariable("id") Long id) {
        UserDetailDTO userDetail = this.userService.handleGetUserDetail(id);
        model.addAttribute("infoUser", userDetail);
        return "admin/user/detail";
    }

    @GetMapping("/admin/user/update/{id}")
    public String getUpdatePage(Model model, @PathVariable long id) {
        UserUpdateRqDTO result = this.userService.handleShowDataUserUpdate(id);
        model.addAttribute("userUpdateRqDTO", result);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdatePage(Model model, @Valid @ModelAttribute("userUpdateRqDTO") UserUpdateRqDTO userUpdateRqDTO,
            BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("avatarFile") MultipartFile file) {
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userUpdateRqDTO" , userUpdateRqDTO);
            return "admin/user/update";
        }

        ResponseBody handleUpdate = this.userService.handleUpdateUser(userUpdateRqDTO , file);
        if ( handleUpdate.getStatus() == 200 ){
            redirectAttributes.addAttribute("messageSuccess" , handleUpdate.getMessage());
            return "redirect:/admin/user";
        }else {
            model.addAttribute("messageError" , handleUpdate.getMessage());
            model.addAttribute("userUpdateRqDTO" ,userUpdateRqDTO);
            return "admin/user/update";
        }
    }
    @GetMapping("/admin/user/delete/{id}")
    public String getDeletePage(Model model, @PathVariable("id") Long id , RedirectAttributes redirectAttributes) {
        ResponseBody response  = this.userService.handleDeleteUser(id);
        if ( response.getStatus() == 200 ){
            redirectAttributes.addFlashAttribute("messageSuccess" , response.getMessage());
        }else {
            redirectAttributes.addFlashAttribute("messageError" , response.getMessage());
        }
        return "redirect:/admin/user";

    }



}
