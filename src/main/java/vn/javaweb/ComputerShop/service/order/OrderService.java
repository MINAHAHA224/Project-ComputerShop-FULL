package vn.javaweb.ComputerShop.service.order;

import jakarta.servlet.http.HttpSession;
import vn.javaweb.ComputerShop.domain.dto.request.OrderUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.momo.MomoRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;

import java.util.List;

 public interface OrderService {
     List<OrderRpDTO> handleGetDataOrderOfUser(HttpSession session) ;
     List<OrderRpDTO> handleGetOrderAd();
     OrderRpDTO handeGetOrderDetailAd(Long id);
     OrderUpdateRqDTO handleGetOrderRqAd(Long id);
     ResponseBody handleUpdateOrderRqAd(OrderUpdateRqDTO orderUpdateRqDTO);
     ResponseBody handleDeleteOrder(Long id);
     ResponseBody handleCompleteOrderPaymentOnline(MomoRpDTO momoRpDTO);
}
