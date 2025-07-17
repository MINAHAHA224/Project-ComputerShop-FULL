package vn.javaweb.ComputerShop.domain.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double totalPrice;

    @Column(name = "receiver_name")
    private String receiverName;
    @Column(name = "receiver_address")
    private String receiverAddress;
    @Column(name = "receiver_phone")
    private String receiverPhone;
    @Column(name = "status")
    private String status;
    @Column(name = "time")
    private Date time;

    @Column(name = "type_payment")
    private String typePayment;

    @Column(name = "status_payment")
    private String statusPayment;

    // user id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "order" , cascade = {CascadeType.REMOVE})
    List<OrderDetailEntity> orderDetails;


}
