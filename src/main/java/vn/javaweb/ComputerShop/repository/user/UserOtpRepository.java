package vn.javaweb.ComputerShop.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.javaweb.ComputerShop.domain.entity.UserEntity;
import vn.javaweb.ComputerShop.domain.entity.UserOtpEntity;

import java.util.List;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtpEntity, Long> {
   List<UserOtpEntity>  findUserOtpEntityByUser (UserEntity user);
}
