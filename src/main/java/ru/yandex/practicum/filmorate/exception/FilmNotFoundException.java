package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends Exception{
    public FilmNotFoundException(final String message) {
        super(message);
    }
}
