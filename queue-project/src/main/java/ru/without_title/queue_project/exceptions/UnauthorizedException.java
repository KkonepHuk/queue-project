package ru.without_title.queue_project.exceptions;

// Исключение для случаев, когда пользователь не авторизован
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
