package vn.javaweb.ComputerShop.handleException;

public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }
}
