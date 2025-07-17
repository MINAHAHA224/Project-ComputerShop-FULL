package vn.javaweb.ComputerShop.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.OrderEntity;
import vn.javaweb.ComputerShop.domain.entity.OrderDetailEntity;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {

    List<OrderDetailEntity> findByOrder(OrderEntity order);

    void deleteByOrder(OrderEntity order);
}
