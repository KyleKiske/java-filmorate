package ru.yandex.practicum.filmorate.exception;

public class FilmAlreadyExistException extends Exception{
    public FilmAlreadyExistException(final String message) {
        super(message);
    }
}
