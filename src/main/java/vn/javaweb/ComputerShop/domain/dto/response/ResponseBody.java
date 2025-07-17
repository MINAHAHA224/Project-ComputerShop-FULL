package vn.javaweb.ComputerShop.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseBody {

    private int status;
    private String message;
    private Object data;

    public ResponseBody(int i, String localizedMessage) {
        this.status = i;
        this.message = localizedMessage;
    }
}
