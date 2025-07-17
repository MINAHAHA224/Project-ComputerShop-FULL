package vn.javaweb.ComputerShop.repository.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.ProductEntity;

@Repository

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, ProductRepositoryCustom {

   void deleteProductEntityById ( Long id);


   boolean existsProductEntityByName ( String name);

   ProductEntity findProductEntityById(long id);


   Page<ProductEntity> findAll(Pageable page);


}