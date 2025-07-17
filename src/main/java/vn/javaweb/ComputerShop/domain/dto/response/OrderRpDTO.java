    package vn.javaweb.ComputerShop.domain.dto.response;

    import lombok.*;

    import java.util.Date;
    import java.util.List;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public class OrderRpDTO {

        private Long id;
        private Double totalPrice;

        private List<OrderDetailRpDTO> orderDetails;

        private String status;


        // detail from show oderAdmin
        private String nameUser;
        private String emailUser;

        private Date time;

        private String typePayment;

        private String statusPayment;


        private String receiverName;

        private String receiverPhone;
        private String receiverAddress;
    }
