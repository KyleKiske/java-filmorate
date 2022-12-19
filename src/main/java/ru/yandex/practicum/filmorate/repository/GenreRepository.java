package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public interface GenreRepository {
    List<Genre> findAll();

    Genre getGenre(int id);
}
