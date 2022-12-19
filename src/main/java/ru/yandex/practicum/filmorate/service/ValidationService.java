package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;

@Service
public class ValidationService {

    public User validateUser(User user){
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")){
            throw new UserValidationException("Введен неверный логин");
        } else if ( user.getBirthday().isAfter(LocalDate.now())) {
            throw new UserValidationException("Введенная дата рождения позже текущего дня");
        }

        if (user.getName() == null || user.getName().isBlank()){
            user.setName(user.getLogin());
        }
        return user;
    }

    public Film validateFilm(Film film){
        if (film.getName().isBlank()){
            throw new FilmValidationException("Название фильма не может быть пустым");
        } else if (film.getDescription().length() > 200){
            throw new FilmValidationException("Длина описания превышает 200 символов");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))){
            throw new FilmValidationException("Дата релиза фильма раньше 28.12.1895");
        } else if (film.getDuration() < 0){
            throw new FilmValidationException("Продолжительность фильма должна быть положительной");
        }
        return film;
    }

}
