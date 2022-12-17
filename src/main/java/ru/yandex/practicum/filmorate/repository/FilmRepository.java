package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilmRepository {
    List<Film> getFilms();

    List<Film> getPopular(int count);

    Optional<Film> getFilmById(int id);

    Film createFilm(Film film);

    boolean updateFilm(Film film);

    void likeFilm(int filmId, long userId);

    void unlikeFilm(int filmId, long userId);
}
