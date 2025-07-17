package vn.javaweb.ComputerShop.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
@Table(name = "cart_detail")
public class CartDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long quantity;

    private double price;

    // cart_id: long
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private CartEntity cart;

    // product_id: long
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;


}
