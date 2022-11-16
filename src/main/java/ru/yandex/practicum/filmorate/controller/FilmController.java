package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        if (films.containsKey(film.getId())){
            try {
                throw new FilmAlreadyExistException("Данный фильм уже добавлен в базу");
            } catch (FilmAlreadyExistException exception){
                log.warn(exception.getMessage());
                return null;
            }
        }

        film = validation(film);
        film.setId(films.size()+1);
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен в библиотеку под id {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public ResponseEntity<Film> putFilm(@RequestBody Film film) {

        if (films.containsKey(film.getId())){
            films.replace(film.getId(), film);
            log.info("Информация о фильме {} под id {} изменена", film.getName(), film.getId());
            return new ResponseEntity<>(film, HttpStatus.OK);
        }

        try {
            throw new FilmNotFoundException("Фильм не найден в базе");
        } catch (FilmNotFoundException exception){
            log.warn(exception.getMessage());
            return new ResponseEntity<>(film, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Film validation(Film film){
        if (film.getName().isBlank()){
            try {
                throw new FilmValidationException("Название фильма не может быть пустым");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getDescription().length() > 200){
            try {
                throw new FilmValidationException("Длина описания превышает 200 символов");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))){
            try {
                throw new FilmValidationException("Дата релиза фильма раньше 28.12.1895");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if (film.getDuration() < 0){
            try {
                throw new FilmValidationException("Продолжительность фильма должна быть положительной");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        }

        return film;
    }

}
