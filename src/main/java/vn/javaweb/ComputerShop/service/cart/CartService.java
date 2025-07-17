package vn.javaweb.ComputerShop.service.cart;

import jakarta.servlet.http.HttpSession;
import vn.javaweb.ComputerShop.domain.dto.request.InfoOrderRqDTO;
import vn.javaweb.ComputerShop.domain.dto.response.CartRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.CheckoutRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;

import java.util.Locale;

 public interface CartService {
     CartRpDTO handleGetCartDetail (HttpSession session);
     ResponseBody handleDeleteProductInCart(Long id , HttpSession session , Locale locale);
     ResponseBody handleAddOneProductToCart(HttpSession session, Long productId , Locale locale);
     CheckoutRpDTO handleShowDataAfterCheckout (HttpSession session);
     ResponseBody handleCreateOrder(HttpSession session , InfoOrderRqDTO infoOrderRqDTO , Locale locale);
     ResponseBody handleAddProductDetailToCart(Long productId, HttpSession session, Long quantity , Locale locale);
}
