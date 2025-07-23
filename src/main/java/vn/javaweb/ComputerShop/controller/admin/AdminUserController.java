package vn.javaweb.ComputerShop.controller.admin;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import vn.javaweb.ComputerShop.utils.ConstantVariable;
import vn.javaweb.ComputerShop.utils.SecurityUtils;


@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    private final UserService userService;

    @GetMapping("/admin/user")
    public String showAllUserPage(Model model) {
        log.info(ConstantVariable.ADMIN_USER + "Start showAllUserPage at {} ", SecurityUtils.currentTime);
        List<UserRpDTO> listUser = this.userService.handleGetUsers();
        model.addAttribute("listUser", listUser);
        log.info(ConstantVariable.ADMIN_USER + "End showAllUserPage at {} ", SecurityUtils.currentTime);
        return "admin/user/show";
    }


    @GetMapping("/admin/user/create")
    public String getCreateUser(Model model) {
        log.info(ConstantVariable.ADMIN_USER + "Start getCreateUser at {} ", SecurityUtils.currentTime);
        model.addAttribute("userCreateRqDTO", new UserCreateRqDTO());
        log.info(ConstantVariable.ADMIN_USER + "End getCreateUser at {} ", SecurityUtils.currentTime);
        return "admin/user/create";
    }

    @PostMapping("/admin/user/create")
    public String postCreateUser(Model model,
                                 @Valid @ModelAttribute("userCreateRqDTO") UserCreateRqDTO userCreateRqDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("avatarFile") MultipartFile file) {
        log.info(ConstantVariable.ADMIN_USER + "Start postCreateUser at {} ", SecurityUtils.currentTime);
        List<FieldError> errors = bindingResult.getFieldErrors();

        if (bindingResult.hasErrors()) {
            for (FieldError error : errors) {
                log.info(">>>> {} - {} ", error.getField(), error.getDefaultMessage());
            }

            model.addAttribute("userCreateRqDTO", userCreateRqDTO);
            log.info(ConstantVariable.ADMIN_USER + "End validation postCreateUser at {} ", SecurityUtils.currentTime);
            return "admin/user/create";
        }
        ResponseBody result = this.userService.handleCreateUser(userCreateRqDTO, file);
        if (result.getStatus() == 200) {
            model.addAttribute("messageSuccess", result.getMessage());
            model.addAttribute("userCreateRqDTO", new UserCreateRqDTO());
            log.info(ConstantVariable.ADMIN_USER + "End success postCreateUser at {} ", SecurityUtils.currentTime);
            return "admin/user/create";
        } else {
            model.addAttribute("messageError", result.getMessage());
            model.addAttribute("userCreateRqDTO", userCreateRqDTO);
            log.info(ConstantVariable.ADMIN_USER + "End error postCreateUser at {} ", SecurityUtils.currentTime);
            return "admin/user/create";
        }

    }


    @GetMapping("/admin/user/{id}")
    public String getDetailUserPage(Model model, @PathVariable("id") Long id) {
        log.info(ConstantVariable.ADMIN_USER + "Start getDetailUserPage at {} - with userId : {} ", SecurityUtils.currentTime, id);
        UserDetailDTO userDetail = this.userService.handleGetUserDetail(id);
        model.addAttribute("infoUser", userDetail);
        log.info(ConstantVariable.ADMIN_USER + "End getDetailUserPage at {} ", SecurityUtils.currentTime);
        return "admin/user/detail";
    }

    @GetMapping("/admin/user/update/{id}")
    public String getUpdateUserPage(Model model, @PathVariable long id) {
        log.info(ConstantVariable.ADMIN_USER + "Start getUpdateUserPage at {} - with userId : {} ", SecurityUtils.currentTime, id);
        UserUpdateRqDTO result = this.userService.handleShowDataUserUpdate(id);
        model.addAttribute("userUpdateRqDTO", result);
        log.info(ConstantVariable.ADMIN_USER + "End getUpdateUserPage at {} ", SecurityUtils.currentTime);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUserPage(Model model, @Valid @ModelAttribute("userUpdateRqDTO") UserUpdateRqDTO userUpdateRqDTO,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                     @RequestParam("avatarFile") MultipartFile file) {
        log.info(ConstantVariable.ADMIN_USER + "Start postUpdateUserPage at {} - with userId : {} ", SecurityUtils.currentTime, userUpdateRqDTO.getId());
        List<FieldError> errors = bindingResult.getFieldErrors();


        if (bindingResult.hasErrors()) {
            log.warn(ConstantVariable.ADMIN_USER + "Validation failed at {} ", SecurityUtils.currentTime);
            for (FieldError error : errors) {
                log.info(">>>> {} - {} ", error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("userUpdateRqDTO", userUpdateRqDTO);
            log.warn(ConstantVariable.ADMIN_USER + "End validation failed at {} ", SecurityUtils.currentTime);
            return "admin/user/update";
        }

        ResponseBody handleUpdate = this.userService.handleUpdateUser(userUpdateRqDTO, file);
        if (handleUpdate.getStatus() == 200) {
            redirectAttributes.addAttribute("messageSuccess", handleUpdate.getMessage());
            log.info(ConstantVariable.ADMIN_USER + "End success postUpdateUserPage at {} ", SecurityUtils.currentTime, userUpdateRqDTO.getId());

            return "redirect:/admin/user";
        } else {
            model.addAttribute("messageError", handleUpdate.getMessage());
            model.addAttribute("userUpdateRqDTO", userUpdateRqDTO);
            log.info(ConstantVariable.ADMIN_USER + "End error postUpdateUserPage at {} ", SecurityUtils.currentTime, userUpdateRqDTO.getId());

            return "admin/user/update";
        }
    }

    @GetMapping("/admin/user/delete/{id}")
    public String getDeletePage(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        log.info(ConstantVariable.ADMIN_USER + "Start getDeletePage at {} - with userId : {} ", SecurityUtils.currentTime, id);
        ResponseBody response = this.userService.handleDeleteUser(id);
        if (response.getStatus() == 200) {
            redirectAttributes.addFlashAttribute("messageSuccess", response.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("messageError", response.getMessage());
        }

        log.info(ConstantVariable.ADMIN_USER + "End getDeletePage at {}  ", SecurityUtils.currentTime);
        return "redirect:/admin/user";

    }


}
