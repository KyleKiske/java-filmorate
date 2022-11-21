package ru.yandex.practicum.filmorate.exception;

public class UserValidationException extends Exception{
    public UserValidationException(final String message) {
        super(message);
    }
}
