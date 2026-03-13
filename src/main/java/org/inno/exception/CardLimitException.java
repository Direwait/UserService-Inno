package org.inno.exception;

public class CardLimitException extends RuntimeException {
    public CardLimitException(String message) {
        super(message);
    }
}
