package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DBFilmRepository implements FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ValidationService validationService;

    public DBFilmRepository(JdbcTemplate jdbcTemplate, ValidationService validationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.validationService = validationService;
    }

    public Optional<Film> mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("FILM_ID");
        return Optional.of(new Film(id, rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new MPA(rs.getInt("MPA"),
                        rs.getString("MPA_NAME")), findGenresByFilmId(id)));
    }

    public List<Genre> findGenresByFilmId(int filmId){
        List<Genre> genres = new ArrayList<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_GENRE AS FG " +
                "JOIN GENRE AS G on G.GENRE_ID = FG.GENRE_ID " +
                "WHERE FILM_ID = ? ORDER BY G.GENRE_ID", filmId);
        while (rs.next()){
            genres.add(new Genre(rs.getInt("GENRE_ID"), rs.getString("NAME")));
        }
        return genres;
    }

    @Override
    public List<Optional<Film>> getFilms() {
        final String sqlQuery = "SELECT FILM.FILM_ID, FILM.NAME, " +
                "DESCRIPTION, RELEASE_DATE, DURATION, MPA, M.MPA_NAME FROM FILM " +
        "JOIN MPA AS M on M.MPA_ID = FILM.MPA";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public List<Optional<Film>> getPopular(int count) {
        final String sqlQuery = "SELECT FILM.FILM_ID, FILM.NAME, " +
                "DESCRIPTION, RELEASE_DATE, DURATION, MPA, M.MPA_NAME FROM FILM " +
                "JOIN MPA AS M on M.MPA_ID = FILM.MPA " +
                "LEFT JOIN LIKES L on FILM.FILM_ID = L.FILM_ID " +
                "GROUP BY FILM.FILM_ID " +
                "ORDER BY COUNT(L.FILM_ID) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        final String sqlQuery = "SELECT FILM.FILM_ID, FILM.NAME, " +
                "DESCRIPTION, RELEASE_DATE, DURATION, MPA, MPA.MPA_NAME, G2.NAME FROM FILM " +
                "JOIN MPA on MPA.MPA_ID = FILM.MPA " +
                "LEFT JOIN FILM_GENRE FG on FILM.FILM_ID = FG.FILM_ID " +
                "LEFT JOIN GENRE G2 on G2.GENRE_ID = FG.GENRE_ID " +
                "WHERE FILM.FILM_ID = ?";
        List<Optional<Film>> filmsOptional = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id);
        List<Film> films = new ArrayList<>();
        for (Optional<Film> f: filmsOptional){
            films.add(f.get());
        }
        if(filmsOptional.size() == 0) {
            return Optional.empty();
        } else if (filmsOptional.size() != 1){
            Film film = films.get(0);
            List<Genre> genres = filterUniqueGenre(films);
            film.setGenres(genres);
            return Optional.of(film);
        } else {
            return filmsOptional.get(0);
        }
    }

    @Override
    public Optional<Film> createFilm(Film film) {
        Film validatedFilm = validationService.validateFilm(film);
        String sqlQuery = "insert into FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA) values (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery, new String[]{"FILM_ID"});
            stmt.setString(1, validatedFilm.getName());
            stmt.setString(2, validatedFilm.getDescription());
            stmt.setDate(3, Date.valueOf(validatedFilm.getReleaseDate()));
            stmt.setInt(4, validatedFilm.getDuration());
            stmt.setInt(5,validatedFilm.getMpa().getId());
            return stmt;
        }, keyHolder);
        if (film.getGenres() != null){
            for (Genre g: film.getGenres()){
                String genreSqlQuery = "INSERT INTO FILM_GENRE (GENRE_ID, FILM_ID)" +
                        "values (?, ?)";
                jdbcTemplate.update(genreSqlQuery, g.getId(), Objects.requireNonNull(keyHolder.getKey()).intValue());
            }
        }
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return Optional.of(film);
    }

    @Override
    public void likeFilm(int filmId, long userId){
        String sqlQuery = "insert into LIKES (FILM_ID, USER_ID) values (?,?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public void unlikeFilm(int filmId, long userId){
        String sqlQuery = "DELETE FROM LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public boolean updateFilm(Optional<Film> optionalFilm) {
        Film film = optionalFilm.get();
        boolean genreChanged;
        String sqlQuery = "update FILM " +
                "set NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA = ? " +
                "where FILM_ID = ?";
        String genreDeleteQuery = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
        genreChanged = jdbcTemplate.update(genreDeleteQuery, film.getId()) > 0;
        if (film.getGenres() != null){
            film.setGenres(filterUniqueGenre(List.of(film)));
            for (Genre g: film.getGenres()){
                String genreSqlQuery = "INSERT INTO FILM_GENRE (GENRE_ID, FILM_ID)" +
                        "values (?, ?) ";
                jdbcTemplate.update(genreSqlQuery, g.getId(), film.getId());
            }
        }
        boolean filmChanged = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) > 0;

        return (filmChanged || genreChanged);
    }

    public List<Genre> filterUniqueGenre(List<Film> films){
        Film film = films.get(0);
        List<Genre> genres = film.getGenres();

        for (int i = 1; i < films.size(); i++){
            genres.add(films.get(i).getGenres().get(0));
        }
        return genres.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
