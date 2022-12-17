package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService){
        this.filmService = filmService;
    }

    @GetMapping
    public List<Optional<Film>> findAll() {
        return filmService.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable int id,
                                           @PathVariable long userId){
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable int id,
                                             @PathVariable long userId){
        filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Optional<Film>> getPopularFilms(@RequestParam(defaultValue = "10") Integer count){
        return filmService.mostPopularFilms(count);
    }

    @PostMapping
    public Optional<Film> create(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @GetMapping("/{id}")
    public Optional<Film> getFilm(@PathVariable int id){
        return filmService.getFilm(id);
    }

    @PutMapping
    public Optional<Film> putFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

}
