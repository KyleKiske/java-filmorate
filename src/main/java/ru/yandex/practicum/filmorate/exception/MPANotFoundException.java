package ru.yandex.practicum.filmorate.exception;

public class MPANotFoundException extends RuntimeException{
    public MPANotFoundException(final String message) {
        super(message);
    }
}