package vn.javaweb.ComputerShop.controller.admin;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.javaweb.ComputerShop.domain.dto.request.OrderUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.service.order.OrderService;
import vn.javaweb.ComputerShop.utils.ConstantVariable;
import vn.javaweb.ComputerShop.utils.SecurityUtils;


@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;


    @GetMapping("/admin/order")
    public String getAllOrder(Model model) {

        log.info(ConstantVariable.ADMIN_ORDER + "Start getAllOrder at = {} ", SecurityUtils.currentTime);

        List<OrderRpDTO> orders = this.orderService.handleGetOrderAd();
        model.addAttribute("orders", orders);

        log.info(ConstantVariable.ADMIN_ORDER + "End getAllOrder at = {} ", SecurityUtils.currentTime);
        return "admin/order/show";
    }

    @GetMapping("/admin/order/{id}")
    public String getOrderDetail(Model model, @PathVariable long id) {
        log.info(ConstantVariable.ADMIN_ORDER + "Start getOrderDetail at {} -  with orderId = {}   ", SecurityUtils.currentTime, id);

        OrderRpDTO order = this.orderService.handeGetOrderDetailAd(id);
        model.addAttribute("order", order);

        log.info(ConstantVariable.ADMIN_ORDER + "End getOrderDetail at {} ", SecurityUtils.currentTime);
        return "admin/order/detail";
    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {
        log.info(ConstantVariable.ADMIN_ORDER + "Start getUpdateOrderPage at {} - with orderId {}  ", SecurityUtils.currentTime, id);
        OrderUpdateRqDTO orders = this.orderService.handleGetOrderRqAd(id);
        model.addAttribute("orders", orders);
        log.info(ConstantVariable.ADMIN_ORDER + "End getUpdateOrderPage at {} ", SecurityUtils.currentTime);

        return "admin/order/update";
    }

    @PostMapping("/admin/order/update")
    public String postUpdateOrderPage(Model model
            , @Valid @ModelAttribute("orders") OrderUpdateRqDTO orderView
            , BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        log.info(ConstantVariable.ADMIN_ORDER + "Start postUpdateOrderPage at {} - with orderId {}  ", SecurityUtils.currentTime, orderView.getId());
        if (bindingResult.hasErrors()) {
            log.warn(ConstantVariable.ADMIN_ORDER + "Validation failed at {} - with orderId = {} ", SecurityUtils.currentTime, orderView.getId());
            model.addAttribute("orders", orderView);
            log.info(ConstantVariable.ADMIN_ORDER + "End postUpdateOrderPage at {} ", SecurityUtils.currentTime);
            return "admin/order/update";
        }

        ResponseBody response = this.orderService.handleUpdateOrderRqAd(orderView);
        if (response.getStatus() != 200) {
            log.error(ConstantVariable.ADMIN_ORDER + "Update failed at {} - orderId = {} ", SecurityUtils.currentTime, orderView.getId());
            model.addAttribute("orders", orderView);
            model.addAttribute("messageError", response.getMessage());
            log.info(ConstantVariable.ADMIN_ORDER + "End error postUpdateOrderPage at {} ", SecurityUtils.currentTime);
            return "admin/order/update";
        } else {
            redirectAttributes.addFlashAttribute("messageSuccess", response.getMessage());
            log.info(ConstantVariable.ADMIN_ORDER + "Update success at {} - orderId = {} ", SecurityUtils.currentTime, orderView.getId());
            log.info(ConstantVariable.ADMIN_ORDER + "End success postUpdateOrderPage at {} ", SecurityUtils.currentTime);
            return "redirect:/admin/order";
        }


    }

    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrderPage(Model model, @PathVariable long id, RedirectAttributes redirectAttributes) {
        log.info(ConstantVariable.ADMIN_ORDER + "Start getDeleteOrderPage at {} - orderId = {} ", SecurityUtils.currentTime, id);
        ResponseBody handleDelete = this.orderService.handleDeleteOrder(id);
        if (handleDelete.getStatus() != 200) {
            log.error(ConstantVariable.ADMIN_ORDER + "Delete failed getDeleteOrderPage at {} - orderId = {} ", SecurityUtils.currentTime, id);
            redirectAttributes.addFlashAttribute("messageError", handleDelete.getMessage());
        } else {
            log.info(ConstantVariable.ADMIN_ORDER + "Delete success getDeleteOrderPage at {} - orderId = {} ", SecurityUtils.currentTime, id);

            redirectAttributes.addFlashAttribute("messageSuccess", handleDelete.getMessage());
        }
        log.info(ConstantVariable.ADMIN_ORDER + "End getDeleteOrderPage at {} ", SecurityUtils.currentTime);
        return "redirect:/admin/order";

    }

}
