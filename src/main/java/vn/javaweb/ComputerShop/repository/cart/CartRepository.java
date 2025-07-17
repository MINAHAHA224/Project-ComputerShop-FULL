package vn.javaweb.ComputerShop.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.CartEntity;
import vn.javaweb.ComputerShop.domain.entity.UserEntity;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    CartEntity findByUser(UserEntity user);
   Optional<CartEntity>  findCartEntityByUser (UserEntity user);

  Optional<CartEntity>  findCartEntityByUserAndStatus ( UserEntity user , String status);

    void deleteCartById(long id);

}
