package vn.javaweb.ComputerShop.service.cart;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.javaweb.ComputerShop.component.MailerComponent;
import vn.javaweb.ComputerShop.component.MessageComponent;
import vn.javaweb.ComputerShop.domain.dto.request.InfoOrderRqDTO;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.dto.response.CartDetailRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.CartRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.CheckoutRpDTO;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;
import vn.javaweb.ComputerShop.domain.entity.*;
import vn.javaweb.ComputerShop.domain.enums.CartStatus;
import vn.javaweb.ComputerShop.domain.enums.OrderStatus;
import vn.javaweb.ComputerShop.domain.enums.PaymentStatus;
import vn.javaweb.ComputerShop.handleException.AuthException;
import vn.javaweb.ComputerShop.handleException.CartException;
import vn.javaweb.ComputerShop.handleException.NotFoundException;
import vn.javaweb.ComputerShop.repository.cart.CartDetailRepository;
import vn.javaweb.ComputerShop.repository.cart.CartRepository;
import vn.javaweb.ComputerShop.repository.order.OrderDetailRepository;
import vn.javaweb.ComputerShop.repository.order.OrderRepository;
import vn.javaweb.ComputerShop.repository.product.ProductRepository;
import vn.javaweb.ComputerShop.repository.user.UserRepository;
import vn.javaweb.ComputerShop.utils.SecurityUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MailerComponent mailerComponent;
    private final MessageComponent messageComponent;

    @Override
    public CartRpDTO handleGetCartDetail(HttpSession session) {
        UserEntity userCurrent = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session))
                .orElseThrow(() -> new AuthException("User not found"));
        CartEntity cart = this.cartRepository.findCartEntityByUserAndStatus(userCurrent, CartStatus.ACTIVE.toString()).orElseThrow(
                () -> new CartException("Cart not found")
        );
        List<CartDetailEntity> cartDetailsOfUser = cart.getCartDetails();
        List<CartDetailRpDTO> listCardDetailRpDTO = cartDetailsOfUser.stream()
                .map(cd ->  CartDetailRpDTO.builder()
                        .id(cd.getId())
                        .price(cd.getPrice())
                        .quantity(cd.getQuantity())
                        .stockQuantity(cd.getProduct().getQuantity())
                        .productId(cd.getProduct().getId())
                        .productName(cd.getProduct().getName())
                        .productImage(cd.getProduct().getImage())
                        .build()
                )
                .collect(Collectors.toList());
        double totalPrice = cartDetailsOfUser.stream()
                .mapToDouble(cd -> cd.getPrice() * cd.getQuantity())
                .sum();

        return new CartRpDTO(listCardDetailRpDTO, totalPrice);
    }


    @Override
    @Transactional
    public ResponseBody handleDeleteProductInCart(Long id, HttpSession session, Locale locale) {
        String email = SecurityUtils.getEmailFromSession(session);
        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);


        UserEntity userCurrent = this.userRepository.findUserEntityByEmail(email).orElseThrow(
                () -> new AuthException("User not found")
        );
        CartEntity cart = this.cartRepository.findCartEntityByUserAndStatus(userCurrent, CartStatus.ACTIVE.toString()).orElseThrow(
                () -> new CartException("Cart not found")
        );
        ProductEntity product = this.productRepository.findProductEntityById(id);
        if ( product == null ){
            throw new NotFoundException("Product not found");
        }
        try {
            this.cartDetailRepository.deleteCartDetailEntityByCartAndProduct(cart, product);

            //set lai sum cua cart trong database
            cart.setSum(cart.getSum() - 1);
            this.cartRepository.save(cart);
        } catch (CartException e) {
            log.warn("--ER  handleDeleteProductInCart {}", e.getMessage());
            throw e;
        }
        // set lai sum trong information va set lai session
        int currentSum = informationDTO.getSum();
        informationDTO.setSum(currentSum - 1);
        session.setAttribute("informationDTO", informationDTO);

        return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.delete.success", locale));
    }

    @Override
    public ResponseBody handleAddOneProductToCart(HttpSession session, Long productId, Locale locale) {
        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);
        int sumCurrent = informationDTO.getSum();

        UserEntity user = this.userRepository.findUserEntityByEmail(SecurityUtils.getEmailFromSession(session)).orElseThrow(
                () -> new AuthException("User not found")
        );

        Optional<CartEntity> cart = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString());
        ProductEntity product = this.productRepository.findProductEntityById(productId);
        if ( product == null ){
            throw new NotFoundException("Product not found");
        }

        if (cart.isEmpty()) {
            CartEntity otherCart = CartEntity.builder()
                    .sum(1)
                    .user(user)
                    .status(CartStatus.ACTIVE.toString())
                    .build();
            CartEntity newCart = this.cartRepository.save(otherCart);

            // set cart detail because first buy but one

            CartDetailEntity cartDetail = CartDetailEntity.builder()
                    .cart(newCart)
                    .product(product)
                    .price(product.getPrice())
                    .quantity(1)
                    .build();
            this.cartDetailRepository.save(cartDetail);

            // reset sum in informationDTO session
            informationDTO.setSum(sumCurrent + 1);
            // set lai session
            session.setAttribute("informationDTO", informationDTO);

            return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.add.success", locale));

        } else {
            CartEntity cartCurrent = cart.get();
            Optional<CartDetailEntity> oldDetail = this.cartDetailRepository.findByCartAndProduct(cart.get(), product);
            if (oldDetail.isEmpty()) {
                CartDetailEntity cartDetail = CartDetailEntity.builder()
                        .cart(cart.get())
                        .product(product)
                        .price(product.getPrice())
                        .quantity(1)
                        .build();
                this.cartDetailRepository.save(cartDetail);

                // update sum in cart khi ma san pham no ko co thi moi +1 hien thi len gio hang
                int sumCurrentInCart = cartCurrent.getSum() + 1;
                cartCurrent.setSum(sumCurrentInCart);
                this.cartRepository.save(cartCurrent);


                // reset sum in informationDTO session
                informationDTO.setSum(sumCurrent + 1);
                // set lai session
                session.setAttribute("informationDTO", informationDTO);

                return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.add.success", locale));
            } else {
                // con san pham no co roi thi thoi khong + 1 hien thi len gio hang cho du no add x10 so luon cua san pham do
                CartDetailEntity cartDetailCurrent = oldDetail.get();
                long quantityCurrent = cartDetailCurrent.getQuantity();
                cartDetailCurrent.setQuantity(quantityCurrent + 1);
                this.cartDetailRepository.save(cartDetailCurrent);

                return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.add.quantityUpdated", locale));
            }
        }
    }

    @Override
    public CheckoutRpDTO handleShowDataAfterCheckout(HttpSession session) {

        String email = SecurityUtils.getEmailFromSession(session);
        UserEntity currentUser = this.userRepository.findUserEntityByEmail(email).orElseThrow(
                () -> new AuthException("User not found")
        );
        CartEntity cart = this.cartRepository.findCartEntityByUserAndStatus(currentUser, CartStatus.ACTIVE.toString()).orElseThrow(
                () -> new CartException("Cart not found")
        );
        List<CartDetailEntity> cartDetails = cart.getCartDetails();

        List<CartDetailRpDTO> listCardDetailRpDTO = cartDetails.stream().map(
                cd -> CartDetailRpDTO.builder()
                        .id(cd.getId())
                        .price(cd.getPrice())
                        .quantity(cd.getQuantity())

                        .productId(cd.getProduct().getId())
                        .productName(cd.getProduct().getName())
                        .productImage(cd.getProduct().getImage())
                        .build()
        ).collect(Collectors.toList());
        double totalPrice = cartDetails.stream().mapToDouble(
                cd -> cd.getPrice() * cd.getQuantity()).sum();


        // show infor user to order place
        InfoOrderRqDTO infoOrderRqDTO = InfoOrderRqDTO.builder()
                .receiverName(currentUser.getFullName().trim())
                .receiverAddress(currentUser.getAddress() != null ? currentUser.getAddress() : "")
                .receiverPhone(currentUser.getPhone() != null ? currentUser.getPhone() : "")
                .totalPriceToSaveOrder(totalPrice)
                .build();


        return new CheckoutRpDTO(listCardDetailRpDTO, totalPrice, infoOrderRqDTO);
    }

    @Override
    @Transactional
    public ResponseBody handleCreateOrder(HttpSession session, InfoOrderRqDTO infoOrderRqDTO, Locale locale) {

        String email = SecurityUtils.getEmailFromSession(session);
        UserEntity user = this.userRepository.findUserEntityByEmail(email).orElseThrow(
                () -> new AuthException("User not found")
        );
        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);


        OrderEntity order = OrderEntity.builder()
                .user(user)
                .receiverName(infoOrderRqDTO.getReceiverName())
                .receiverAddress(infoOrderRqDTO.getReceiverAddress())
                .receiverPhone(infoOrderRqDTO.getReceiverPhone())
                .totalPrice(infoOrderRqDTO.getTotalPriceToSaveOrder())
                .status(OrderStatus.PENDING.toString())
                .time(new Date())
                .typePayment(infoOrderRqDTO.getPaymentMethod())
                .statusPayment(PaymentStatus.UNPAID.toString())
                .build();

        OrderEntity orderNew = this.orderRepository.save(order);

        // create orderDetail

        // step 1: get cart by user
        CartEntity cartCurrent = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString()).orElseThrow(
                () -> new CartException("Cart not found")
        );
        List<CartDetailEntity> cartDetails = cartCurrent.getCartDetails();
        // step 2: handle update data
        cartDetails.forEach(
                cd -> {
                    OrderDetailEntity orderDetail = new OrderDetailEntity();
                    orderDetail.setOrder(orderNew);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice() * cd.getQuantity());
                    orderDetail.setQuantity(cd.getQuantity());
                    this.orderDetailRepository.save(orderDetail);

                    // set lai quantity cho product
                    ProductEntity productCurrent = cd.getProduct();
                    productCurrent.setSold(productCurrent.getSold() + cd.getQuantity());
                    this.productRepository.save(productCurrent);
                }
        );


        // step 2: update status of card
        if (infoOrderRqDTO.getPaymentMethod().equals("COD")) {
            cartCurrent.setStatus(CartStatus.ORDERED.toString());
        }
        this.cartRepository.save(cartCurrent);

        // step 3 : update session
        informationDTO.setSum(0);
        session.setAttribute("informationDTO", informationDTO);

        if (infoOrderRqDTO.getPaymentMethod().equals("COD")) {
            mailerComponent.sendInvoiceEmail(orderNew);
        }

        return new ResponseBody(200, messageComponent.getLocalizedMessage("order.create.success", locale), orderNew);

    }

    @Override
    public ResponseBody handleAddProductDetailToCart(Long productId, HttpSession session, Long quantity, Locale locale) {

        InformationDTO informationDTO = SecurityUtils.getInformationDtoFromSession(session);
        int sumCurrent = informationDTO.getSum();
        String email = SecurityUtils.getEmailFromSession(session);
        UserEntity user = this.userRepository.findUserEntityByEmail(email).orElseThrow(
                () -> new AuthException("User not found")
        );

        Optional<CartEntity> cart = this.cartRepository.findCartEntityByUserAndStatus(user, CartStatus.ACTIVE.toString());
        ProductEntity product = this.productRepository.findProductEntityById(productId);
        if ( product == null ){
            throw new NotFoundException("Product not found");
        }
        if (cart.isEmpty()) {
            CartEntity otherCart = new CartEntity();
            otherCart.setSum(1);
            otherCart.setUser(user);
            otherCart.setStatus(CartStatus.ACTIVE.toString());
            CartEntity newCart = this.cartRepository.save(otherCart);

            // set cart detail because first buy but one

            CartDetailEntity cartDetail = new CartDetailEntity();
            cartDetail.setCart(newCart);
            cartDetail.setProduct(product);
            cartDetail.setPrice(product.getPrice());
            cartDetail.setQuantity((int) quantity.longValue());
            this.cartDetailRepository.save(cartDetail);

            // reset sum in informationDTO session
            informationDTO.setSum(sumCurrent + 1);
            // set lai session
            session.setAttribute("informationDTO", informationDTO);


            return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.delete.success", locale));

        } else {
            CartEntity cartCurrent = cart.get();
            Optional<CartDetailEntity> oldDetail = this.cartDetailRepository.findByCartAndProduct(cart.get(), product);
            if (oldDetail.isEmpty()) {
                CartDetailEntity cartDetail = new CartDetailEntity();
                cartDetail.setCart(cart.get());
                cartDetail.setProduct(product);
                cartDetail.setPrice(product.getPrice());
                cartDetail.setQuantity((int) quantity.longValue());
                this.cartDetailRepository.save(cartDetail);

                // update sum in cart khi ma san pham no ko co thi moi +1 hien thi len gio hang

                cartCurrent.setSum(cartCurrent.getSum() + 1);
                this.cartRepository.save(cartCurrent);


                // reset sum in informationDTO session
                informationDTO.setSum(sumCurrent + 1);
                // set lai session
                session.setAttribute("informationDTO", informationDTO);

                return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.add.success", locale));
            } else {
                // con san pham no co roi thi thoi khong + 1 hien thi len gio hang cho du no add x10 so luon cua san pham do
                CartDetailEntity cartDetailCurrent = oldDetail.get();
                long quantityCurrent = cartDetailCurrent.getQuantity();
                cartDetailCurrent.setQuantity(quantityCurrent + (int) quantity.longValue());
                this.cartDetailRepository.save(cartDetailCurrent);


                return new ResponseBody(200, messageComponent.getLocalizedMessage("cart.product.add.quantityUpdated", locale));
            }
        }


    }

}
