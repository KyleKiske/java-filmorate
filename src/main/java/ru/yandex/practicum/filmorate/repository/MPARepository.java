package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@Repository
public interface MPARepository {
    List<MPA> findAll();

    MPA getMPA(int id);
}
