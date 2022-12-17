package ru.yandex.practicum.filmorate.exception;

public class FilmValidationException extends RuntimeException{
    public FilmValidationException(final String message) {
        super(message);
    }
}
