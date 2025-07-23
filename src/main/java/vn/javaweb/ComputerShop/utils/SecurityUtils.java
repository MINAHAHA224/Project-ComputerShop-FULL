package vn.javaweb.ComputerShop.utils;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.javaweb.ComputerShop.domain.dto.request.InformationDTO;
import vn.javaweb.ComputerShop.domain.entity.UserEntity;

public class SecurityUtils {
    public static String getPrincipal() {
        return  ((UserEntity)SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getEmail();
    }

    public static InformationDTO getInformationDtoFromSession (HttpSession session) {
        return (InformationDTO)session.getAttribute("informationDTO");
    }



    public static String getEmailFromSession(HttpSession session){
        return (String)session.getAttribute("email");
    }

    public static final Long currentTime = System.currentTimeMillis();

}
