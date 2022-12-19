package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DBFilmRepository;
import ru.yandex.practicum.filmorate.dao.DBUserRepository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    @Autowired
    public FilmService(DBFilmRepository filmRepository, DBUserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public List<Film> findAll() {
            return filmRepository.getFilms().stream()
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
    }

    public Film getFilm(int id){
        Optional<Film> film = filmRepository.getFilmById(id);
        if (film.isPresent()) {
            return film.get();
        } else {
            throw new FilmNotFoundException("Фильм с id " + id + " не найден");
        }
    }

    public Film createFilm(Film film) {
        return filmRepository.createFilm(film);
    }

    public Film updateFilm(Film film) {
        boolean changed = filmRepository.updateFilm(Optional.ofNullable(film));
        if (!changed){
            assert film != null;
            log.warn("Фильм " + film.getId() + " не найден в базе");
            throw new FilmNotFoundException("Фильм " + film.getId() + " не найден в базе");
        }
        assert film != null;
        log.info("Информация о фильме {} под id {} изменена", film.getName(), film.getId());
        return film;
    }

    public void likeFilm(int filmId, long userId){
        if (userRepository.getUserById(userId).isPresent()){
            filmRepository.likeFilm(filmId, userId);
        } else {
            throw new UserNotFoundException("Пользователь с id " + userId + "не найден.");
        }
    }

    public void unlikeFilm(int filmId, long userId){
        if (userRepository.getUserById(userId).isPresent()){
            filmRepository.unlikeFilm(filmId, userId);
        } else {
            throw new UserNotFoundException("Пользователь с id " + userId + "не найден.");
        }
    }

    public List<Film> mostPopularFilms(int count){
        return filmRepository.getPopular(count).stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

}
