package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    public ResponseEntity<Film> getFilm(int id){
        Film film = filmStorage.getFilms().get(id);
        if (film == null){
            try {
                throw new FilmNotFoundException("Фильм с id " + id + " не найден");
            } catch (FilmNotFoundException exception){
                log.warn(exception.getMessage());
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    public List<Film> findAll() {
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public ResponseEntity<Film> create(@RequestBody Film film) {
        if (filmStorage.getFilms().containsKey(film.getId())){
            try {
                throw new FilmAlreadyExistException("Данный фильм уже добавлен в базу");
            } catch (FilmAlreadyExistException exception){
                log.warn(exception.getMessage());
                return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
            }
        }

        String result = validation(film);
        if (result == null){
            film.setId(filmStorage.getFilms().size()+1);
            filmStorage.getFilms().put(film.getId(), film);
            log.info("Фильм {} добавлен в библиотеку под id {}", film.getName(), film.getId());
            return new ResponseEntity<>(film, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Film> putFilm(@RequestBody Film film) {
        Set<Long> likes = filmStorage.getFilms().get(film.getId()).getLikes();
        if (filmStorage.getFilms().containsKey(film.getId())){
            filmStorage.getFilms().replace(film.getId(), film);
            filmStorage.getFilms().get(film.getId()).setLikes(likes);
            log.info("Информация о фильме {} под id {} изменена", film.getName(), film.getId());
            return new ResponseEntity<>(film, HttpStatus.OK);
        }
        log.warn("Фильм" + film.getId() + "не найден в базе");
        return new ResponseEntity<>(film, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<String> likeFilm(int filmId, long userId){
        if (userStorage.getUsers().containsKey(userId)){
            filmStorage.getFilms().get(filmId).getLikes().add(userId);
        } else {
            try {
                throw new UserNotFoundException("Пользователь с id " + userId + "не найден.");
            } catch (UserNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    public ResponseEntity<String> unlikeFilm(int filmId, long userId){
        if (userStorage.getUsers().containsKey(userId)){
            filmStorage.getFilms().get(filmId).getLikes().remove(userId);
        } else {
            try {
                throw new UserNotFoundException("Пользователь с id " + userId + "не найден.");
            } catch (UserNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    public List<Film> mostPopularFilms(@RequestParam Integer count){
        return filmStorage.getFilms().values()
                .stream()
                .sorted(Film::compareTo)
                .limit(count)
                .collect(Collectors.toList());
    }

    public String validation(Film film){
        if (film.getName().isBlank()){
            try {
                throw new FilmValidationException("Название фильма не может быть пустым");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        } else if (film.getDescription().length() > 200){
            try {
                throw new FilmValidationException("Длина описания превышает 200 символов");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))){
            try {
                throw new FilmValidationException("Дата релиза фильма раньше 28.12.1895");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        } else if (film.getDuration() < 0){
            try {
                throw new FilmValidationException("Продолжительность фильма должна быть положительной");
            } catch (FilmValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        }

        return null;
    }
}
