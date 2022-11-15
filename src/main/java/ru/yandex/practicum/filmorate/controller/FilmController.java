package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    private final HashSet<Film> films = new HashSet<>();

    @GetMapping
    public HashSet<Film> findAll() {
        return films;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        for (Film filmFromSet: films){
            if (filmFromSet.equals(film)){
                try {
                    throw new FilmController.FilmAlreadyExistException("Данный фильм уже добавлен в базу");
                } catch (FilmController.FilmAlreadyExistException exception){
                    log.warn(exception.getMessage());
                    return null;
                }
            }
        }

        film = validation(film);
        film.setId(films.size()+1);
        films.add(film);
        log.info("Фильм {} добавлен в библиотеку под id {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public ResponseEntity<Film> putFilm(@RequestBody Film film) {

        for (Film filmFromList: films){
            if (filmFromList.equals(film)){
                filmFromList.setDuration(film.getDuration());
                filmFromList.setName(film.getName());
                filmFromList.setReleaseDate(film.getReleaseDate());
                filmFromList.setDescription(film.getDescription());
                log.info("Информация о фильме {} под id {} изменена", film.getName(), film.getId());
                return new ResponseEntity<>(film, HttpStatus.OK);
            }
        }

        try {
            throw new FilmController.FilmNotFoundException("Фильм не найден в базу");
        } catch (FilmController.FilmNotFoundException exception){
            log.warn(exception.getMessage());
            return new ResponseEntity<>(film, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Film validation(Film film){
        if (film.getName().isBlank()){
            try {
                throw new FilmController.FilmValidationException("Название фильма не может быть пустым");
            } catch (FilmController.FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getDescription().length() > 200){
            try {
                throw new FilmController.FilmValidationException("Длина описания превышает 200 символов");
            } catch (FilmController.FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))){
            try {
                throw new FilmController.FilmValidationException("Дата релиза фильма раньше 28.12.1895");
            } catch (FilmController.FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getDuration() < 0){
            try {
                throw new FilmController.FilmValidationException("Продолжительность фильма должна быть положительной");
            } catch (FilmController.FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        }

        return film;
    }

    static class FilmValidationException extends Exception{
        public FilmValidationException(final String message) {
            super(message);
        }
    }

    static class FilmNotFoundException extends Exception{
        public FilmNotFoundException(final String message) {
            super(message);
        }
    }

    static class FilmAlreadyExistException extends Exception{
        public FilmAlreadyExistException(final String message) {
            super(message);
        }
    }
}
