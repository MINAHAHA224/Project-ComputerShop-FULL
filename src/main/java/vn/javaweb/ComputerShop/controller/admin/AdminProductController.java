package vn.javaweb.ComputerShop.controller.admin;

import java.util.List;
import java.util.Optional;

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
import vn.javaweb.ComputerShop.domain.dto.request.ProductCreateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.ProductUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductDetailRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ProductFilterAdRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.service.product.ProductService;

import vn.javaweb.ComputerShop.service.upload.UploadService;

@Controller
@RequiredArgsConstructor
public class AdminProductController {
    private final UploadService uploadService;
    private final ProductService productService;



    @GetMapping("/admin/product")
    public String getProduct(Model model, @RequestParam("page") Optional<String> pageOptional) {

        ProductFilterAdRpDTO result = this.productService.handleShowDataProductAdmin(pageOptional);
        model.addAttribute("listProduct",result.getListProduct());
        model.addAttribute("currentPage", result.getPage());
        model.addAttribute("totalPages", result.getTotalPage());

        return "admin/product/show";
    }

    // Create product not update

    @GetMapping("/admin/product/create")
    public String getCreateProduct(Model model) {
        model.addAttribute("productCreateRqDTO", new ProductCreateRqDTO());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String postCreateProduct(Model model, @Valid @ModelAttribute("productCreateRqDTO") ProductCreateRqDTO productCreateRqDTO,
            BindingResult bindingResult,
            @RequestParam("avatarFile") MultipartFile file) {
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("productCreateRqDTO" ,productCreateRqDTO );
            return "admin/product/create";
        }


        ResponseBody response = this.productService.handleCreateProduct(productCreateRqDTO , file);
       if ( response.getStatus() == 200 ){
           model.addAttribute("messageSuccess" , response.getMessage());
           model.addAttribute("productCreateRqDTO" , new ProductCreateRqDTO());
           return "admin/product/create";
       }else {
           model.addAttribute("messageError" , response.getMessage());
           model.addAttribute("productCreateRqDTO" , productCreateRqDTO);
           return "admin/product/create";
       }

    }

    // Product detail
    @GetMapping("/admin/product/{id}")
    public String getProductDetail(Model model, @PathVariable("id") Long id) {
        ProductDetailRpDTO productDetail = this.productService.handleGetProductRpAdmin(id);
        model.addAttribute("product", productDetail);
        return "admin/product/detail";
    }

    // product update
    @GetMapping("/admin/product/update/{id}")
    public String getProductUpdate(Model model, @PathVariable("id") Long id) {
        ProductUpdateRqDTO productUpdateRqDTO = this.productService.handleGetProductUpdate(id);
        model.addAttribute("productUpdateRqDTO", productUpdateRqDTO);
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String postProductUpdate(Model model , @Valid @ModelAttribute("productUpdateRqDTO") ProductUpdateRqDTO productUpdateRqDTO,
            BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("avatarFile") MultipartFile file) {

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("productUpdateRqDTO" , productUpdateRqDTO);
            return "admin/product/update";
        }
        ResponseBody response = this.productService.handleUpdateProduct(productUpdateRqDTO , file);
        if ( response.getStatus() == 200 ){
            redirectAttributes.addFlashAttribute("messageSuccess" , response.getMessage());
            return "redirect:/admin/product";
        }else {
            model.addAttribute("messageError" , response.getMessage());
            model.addAttribute("productUpdateRqDTO" , productUpdateRqDTO);
            return "admin/product/update";
        }

    }

    // product delete

    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProduct(Model model, @PathVariable("id") Long id , RedirectAttributes redirectAttributes) {

        ResponseBody deleteProduct = this.productService.handleDeleteProduct(id);
        if ( deleteProduct.getStatus() == 200 ){
            redirectAttributes.addFlashAttribute("messageSuccess" , deleteProduct.getMessage());
        }else {
            redirectAttributes.addFlashAttribute("messageError" , deleteProduct.getMessage());
        }
        return "redirect:/admin/product";
    }



}
