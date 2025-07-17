package vn.javaweb.ComputerShop.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {



    OrderEntity findOrderEntityById(Long id);

    void deleteOrderEntityById ( Long id);


}
