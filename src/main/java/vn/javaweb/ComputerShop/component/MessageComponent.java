package vn.javaweb.ComputerShop.component;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageComponent {

    private final MessageSource messageSource;

    public  Locale getLocalFromSession (HttpSession session){
        String localSession = (String) session.getAttribute("session_locale");
        if ( localSession.equals("en_US")){
            return new Locale("en" , "US");
        }else {
            return new Locale("vi" , "VN");
        }
    }
    public String getLocalizedMessage(String messageId, Locale locale) {
        return messageSource.getMessage(messageId, null, locale);
    }
}
