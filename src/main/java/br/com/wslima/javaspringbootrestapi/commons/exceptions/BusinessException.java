package br.com.wslima.javaspringbootrestapi.commons.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
