package vn.javaweb.ComputerShop.service.order;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import vn.javaweb.ComputerShop.component.MailerComponent;
import vn.javaweb.ComputerShop.domain.dto.request.OrderUpdateRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.momo.MomoRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderDetailRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.OrderRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.domain.entity.*;
import vn.javaweb.ComputerShop.domain.enums.CartStatus;
import vn.javaweb.ComputerShop.domain.enums.PaymentStatus;
import vn.javaweb.ComputerShop.handleException.AuthException;
import vn.javaweb.ComputerShop.handleException.NotFoundException;
import vn.javaweb.ComputerShop.repository.cart.CartRepository;
import vn.javaweb.ComputerShop.repository.order.OrderRepository;
import vn.javaweb.ComputerShop.repository.user.UserRepository;
import vn.javaweb.ComputerShop.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final MailerComponent mailerComponent;

   @Override
    public List<OrderRpDTO> handleGetDataOrderOfUser(HttpSession session) {
        UserEntity user = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session))
                .orElseThrow(() -> new AuthException("User not found"));
        List<OrderEntity> listOrder = user.getOrders();
       List<OrderRpDTO> listResult =  listOrder.stream().map(
               od -> {
                   List<OrderDetailRpDTO> listOrderDetailRp = od.getOrderDetails().stream()
                           .map(odd ->
                               OrderDetailRpDTO.builder()
                                       .productId(odd.getProduct().getId())
                                       .productName(odd.getProduct().getName())
                                       .productImage(odd.getProduct().getImage())
                                       .productQuantity(odd.getQuantity())
                                       .price(odd.getPrice())
                                       .build()
                           ).toList();
                   OrderRpDTO result = OrderRpDTO.builder()
                           .id(od.getId())
                           .totalPrice(od.getTotalPrice())
                           .status(od.getStatus())
                           .time(od.getTime())
                           .orderDetails(listOrderDetailRp)
                           .build();
                   return result;
               }
       ).collect(Collectors.toList());
        return listResult;
    }
    @Override
    public List<OrderRpDTO> handleGetOrderAd() {
        List<OrderEntity> listEntity = this.orderRepository.findAll();
        return listEntity.stream().map(
                od ->
                        OrderRpDTO.builder()
                                .id(od.getId())
                                .nameUser(od.getReceiverName())
                                .totalPrice(od.getTotalPrice())
                                .status(od.getStatus())
                                .time(od.getTime())
                                .typePayment(od.getTypePayment())
                                .statusPayment(od.getStatusPayment())
                                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public OrderRpDTO handeGetOrderDetailAd(Long id) {
        OrderEntity orderEntity = this.orderRepository.findOrderEntityById(id);
        if ( orderEntity == null ){
            throw new NotFoundException("Order not found");
        }
        List<OrderDetailRpDTO> listResult = orderEntity.getOrderDetails().stream().map(
                odd ->
                        OrderDetailRpDTO.builder()
                                .productId(odd.getId())
                                .productName(odd.getProduct().getName())
                                .productImage(odd.getProduct().getImage())
                                .price(odd.getPrice())
                                .productQuantity(odd.getQuantity())
                                .build()
        ).collect(Collectors.toList());

        return OrderRpDTO.builder()
                .id(orderEntity.getId())
                .time(orderEntity.getTime())
                .totalPrice(orderEntity.getTotalPrice())
                .orderDetails(listResult)
                .status(orderEntity.getStatus())
                .statusPayment(orderEntity.getStatusPayment())
                .nameUser(orderEntity.getUser().getFullName())
                .emailUser(orderEntity.getUser().getEmail())
                .receiverName(orderEntity.getReceiverName())
                .receiverPhone(orderEntity.getReceiverPhone())
                .receiverAddress(orderEntity.getReceiverAddress())
                .build();
    }


    @Override
    public OrderUpdateRqDTO handleGetOrderRqAd(Long id) {
        OrderEntity order = this.orderRepository.findOrderEntityById(id);
        if ( order == null ){
            throw new NotFoundException("Order not found");
        }
        return OrderUpdateRqDTO.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .typePayment(order.getTypePayment())
                .statusPayment(order.getStatusPayment())
                .receiverPhone(order.getReceiverPhone())
                .receiverName(order.getReceiverName())
                .receiverAddress(order.getReceiverAddress())
                .build();
    }


    @Override
    @Transactional
    public ResponseBody handleUpdateOrderRqAd(OrderUpdateRqDTO orderUpdateRqDTO) {
        OrderEntity order = this.orderRepository.findOrderEntityById(orderUpdateRqDTO.getId());
        if ( order == null ){
            throw new NotFoundException("Order not found");
        }
        order.setStatus(orderUpdateRqDTO.getStatus());
        order.setReceiverName(orderUpdateRqDTO.getReceiverName());
        order.setReceiverPhone(orderUpdateRqDTO.getReceiverPhone());
        order.setReceiverAddress(orderUpdateRqDTO.getReceiverAddress());
        order.setStatusPayment(orderUpdateRqDTO.getStatusPayment());
        this.orderRepository.save(order);
        return new ResponseBody(200 ,"Admin : Cập nhật trạng đơn đặt hàng thành công" );
    }

    @Override
    @Transactional
    public ResponseBody handleDeleteOrder(Long id) {
        Optional<OrderEntity> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy đơn hàng với ID: " + id);
        }
        this.orderRepository.deleteOrderEntityById(id);
        return new ResponseBody(200 ,"Admin : Xóa đơn hàng thành công" );
    }

    @Override
    @Transactional
    public ResponseBody handleCompleteOrderPaymentOnline(MomoRpDTO momoRpDTO) {
        ResponseBody response = new ResponseBody();

        String prefixOrderId = momoRpDTO.getOrderId().substring(0, 2);
        Long orderId = Long.valueOf(prefixOrderId);
        OrderEntity orderEntity = this.orderRepository.findOrderEntityById(orderId);
        if ( orderEntity == null ){
            throw new NotFoundException("Order not found");
        }
        if (momoRpDTO.getResultCode() != 1006) {

            UserEntity user = orderEntity.getUser();
            CartEntity cart = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString()).orElseThrow(
                    () -> new NotFoundException("Cart not found")
            );
            cart.setStatus(CartStatus.ORDERED.toString());
            this.cartRepository.save(cart);
            orderEntity.setStatusPayment(PaymentStatus.PAID.toString());
            this.orderRepository.save(orderEntity);

            response.setStatus(200);
            response.setMessage(momoRpDTO.getMessage());

            // send mail invoice
            mailerComponent.sendInvoiceEmail(orderEntity);
        } else {
            this.orderRepository.deleteOrderEntityById(orderEntity.getId());
            response.setStatus(500);
            response.setMessage(momoRpDTO.getMessage());

        }


        return response;
    }


}
