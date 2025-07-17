package vn.javaweb.ComputerShop.domain.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "carts")
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Min(value = 0)
    private int sum;


    private String status;

    // user_id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // cart_detail_id
    @OneToMany(mappedBy = "cart")
    List<CartDetailEntity> cartDetails;



}
