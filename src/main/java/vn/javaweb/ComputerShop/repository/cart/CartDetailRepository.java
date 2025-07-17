package vn.javaweb.ComputerShop.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.CartDetailEntity;
import vn.javaweb.ComputerShop.domain.entity.CartEntity;
import vn.javaweb.ComputerShop.domain.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetailEntity, Long> {

    CartDetailEntity findCartDetailEntityById ( Long id);
    boolean existsByCartAndProduct(CartEntity cart, ProductEntity product);

    Optional<CartDetailEntity>   findByCartAndProduct(CartEntity cart, ProductEntity product);

    List<CartDetailEntity> findByCart(CartEntity cart);

    CartDetailEntity findByProductId(long id);

    void deleteCDetailById(long id);

    void deleteCartDetailEntityByCartAndProduct ( CartEntity cart , ProductEntity product);

    boolean existsByProduct(ProductEntity product);

}