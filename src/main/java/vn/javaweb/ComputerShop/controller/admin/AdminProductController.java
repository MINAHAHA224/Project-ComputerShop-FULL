package vn.javaweb.ComputerShop.controller.admin;

import java.util.List;
import java.util.Optional;

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
import vn.javaweb.ComputerShop.domain.dto.request.ProductCreateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductDetailRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductFilterAdRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.service.product.ProductService;

import vn.javaweb.ComputerShop.utils.ConstantVariable;
import vn.javaweb.ComputerShop.utils.SecurityUtils;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {
    private final ProductService productService;


    @GetMapping("/admin/product")
    public String getAllProduct(Model model, @RequestParam("page") Optional<String> pageOptional) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start getAllProduct at {} ", SecurityUtils.currentTime);

        ProductFilterAdRpDTO result = this.productService.handleShowDataProductAdmin(pageOptional);
        model.addAttribute("listProduct", result.getListProduct());
        model.addAttribute("currentPage", result.getPage());
        model.addAttribute("totalPages", result.getTotalPage());

        log.info(ConstantVariable.ADMIN_PRODUCT + "End getAllProduct at {} ", SecurityUtils.currentTime);
        return "admin/product/show";
    }

    // Create product not update

    @GetMapping("/admin/product/create")
    public String getCreateProduct(Model model) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start getCreateProduct at {} ", SecurityUtils.currentTime);
        model.addAttribute("productCreateRqDTO", new ProductCreateRqDTO());
        log.info(ConstantVariable.ADMIN_PRODUCT + "End getCreateProduct at {} ", SecurityUtils.currentTime);
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String postCreateProduct(Model model, @Valid @ModelAttribute("productCreateRqDTO") ProductCreateRqDTO productCreateRqDTO,
                                    BindingResult bindingResult,
                                    @RequestParam("avatarFile") MultipartFile file) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start postCreateProduct at {} ", SecurityUtils.currentTime);
        List<FieldError> errors = bindingResult.getFieldErrors();


        if (bindingResult.hasErrors()) {
            log.warn(ConstantVariable.ADMIN_PRODUCT + "Validation failed at {} ", SecurityUtils.currentTime);
            for (FieldError error : errors) {
                log.warn(">>>> {} - {} ", error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("productCreateRqDTO", productCreateRqDTO);
            log.info(ConstantVariable.ADMIN_PRODUCT + "End postCreateProduct at {} ", SecurityUtils.currentTime);
            return "admin/product/create";
        }


        ResponseBody response = this.productService.handleCreateProduct(productCreateRqDTO, file);
        if (response.getStatus() == 200) {
            model.addAttribute("messageSuccess", response.getMessage());
            model.addAttribute("productCreateRqDTO", new ProductCreateRqDTO());
            log.info(ConstantVariable.ADMIN_PRODUCT + "End success postCreateProduct at {} ", SecurityUtils.currentTime);
            return "admin/product/create";
        } else {
            model.addAttribute("messageError", response.getMessage());
            model.addAttribute("productCreateRqDTO", productCreateRqDTO);
            log.info(ConstantVariable.ADMIN_PRODUCT + "End error postCreateProduct at {} ", SecurityUtils.currentTime);
            return "admin/product/create";
        }


    }

    // Product detail
    @GetMapping("/admin/product/{id}")
    public String getProductDetail(Model model, @PathVariable("id") Long id) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start getProductDetail at {} - with productId : {} ", SecurityUtils.currentTime, id);
        ProductDetailRpDTO productDetail = this.productService.handleGetProductRpAdmin(id);
        model.addAttribute("product", productDetail);
        log.info(ConstantVariable.ADMIN_PRODUCT + "End getProductDetail at {} ", SecurityUtils.currentTime);
        return "admin/product/detail";
    }

    // product update
    @GetMapping("/admin/product/update/{id}")
    public String getProductUpdate(Model model, @PathVariable("id") Long id) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start getProductUpdate at {} - with productId : {} ", SecurityUtils.currentTime, id);
        ProductUpdateRqDTO productUpdateRqDTO = this.productService.handleGetProductUpdate(id);
        model.addAttribute("productUpdateRqDTO", productUpdateRqDTO);
        log.info(ConstantVariable.ADMIN_PRODUCT + "End getProductUpdate at {} ", SecurityUtils.currentTime);
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String postProductUpdate(Model model, @Valid @ModelAttribute("productUpdateRqDTO") ProductUpdateRqDTO productUpdateRqDTO,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                    @RequestParam("avatarFile") MultipartFile file) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start postProductUpdate at {} - with productId : {} ", SecurityUtils.currentTime, productUpdateRqDTO.getId());
        List<FieldError> errors = bindingResult.getFieldErrors();


        if (bindingResult.hasErrors()) {
            log.warn(ConstantVariable.ADMIN_PRODUCT + "Validation failed at {} ", SecurityUtils.currentTime);
            for (FieldError error : errors) {
                log.warn(">>>> {} - {} ", error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("productUpdateRqDTO", productUpdateRqDTO);
            log.info(ConstantVariable.ADMIN_PRODUCT + "End postProductUpdate at {} ", SecurityUtils.currentTime);
            return "admin/product/update";
        }
        ResponseBody response = this.productService.handleUpdateProduct(productUpdateRqDTO, file);
        if (response.getStatus() == 200) {
            redirectAttributes.addFlashAttribute("messageSuccess", response.getMessage());
            log.info(ConstantVariable.ADMIN_PRODUCT + "End success postProductUpdate at {} ", SecurityUtils.currentTime);
            return "redirect:/admin/product";
        } else {
            model.addAttribute("messageError", response.getMessage());
            model.addAttribute("productUpdateRqDTO", productUpdateRqDTO);
            log.info(ConstantVariable.ADMIN_PRODUCT + "End error postProductUpdate at {} ", SecurityUtils.currentTime);
            return "admin/product/update";
        }

    }

    // product delete

    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProduct(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        log.info(ConstantVariable.ADMIN_PRODUCT + "Start getDeleteProduct at {} - with productId : {} ", SecurityUtils.currentTime, id);
        ResponseBody deleteProduct = this.productService.handleDeleteProduct(id);
        if (deleteProduct.getStatus() == 200) {
            redirectAttributes.addFlashAttribute("messageSuccess", deleteProduct.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("messageError", deleteProduct.getMessage());
        }
        log.info(ConstantVariable.ADMIN_PRODUCT + "End getDeleteProduct at {}  ", SecurityUtils.currentTime);

        return "redirect:/admin/product";
    }


}
