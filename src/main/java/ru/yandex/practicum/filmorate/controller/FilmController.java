package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService){
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<String> likeFilm(@PathVariable int id,
                                           @PathVariable long userId){
        return filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<String> unlikeFilm(@PathVariable int id,
                                             @PathVariable long userId){
        return filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count){
        return filmService.mostPopularFilms(count);
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilm(@PathVariable int id){
        return filmService.getFilm(id);
    }

    @PutMapping
    public ResponseEntity<Film> putFilm(@RequestBody Film film) {
        return filmService.putFilm(film);
    }

}
