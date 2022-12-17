package ru.yandex.practicum.filmorate.exception;

public class UserValidationException extends RuntimeException{
    public UserValidationException(final String message) {
        super(message);
    }
}
