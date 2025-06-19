package br.com.wslima.javaspringbootrestapi.commons.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
