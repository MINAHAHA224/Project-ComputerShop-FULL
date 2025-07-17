package vn.javaweb.ComputerShop.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.javaweb.ComputerShop.domain.entity.UserEntity;

@Repository

public interface UserRepository extends JpaRepository<UserEntity, Long> {

   Optional<UserEntity>  findUserEntityByEmail(String email);
   boolean existsUserEntityByEmail ( String email);

   boolean existsUserEntityByPhone ( String phone);

   void deleteUserEntityById ( Long id);

   UserEntity findUserEntityById ( Long id);




    boolean existsByEmail(String email);

}
