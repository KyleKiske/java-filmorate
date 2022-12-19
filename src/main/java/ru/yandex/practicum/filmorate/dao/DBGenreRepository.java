package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class DBGenreRepository implements GenreRepository {

    private final JdbcTemplate jdbcTemplate;

    public DBGenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Genre mapRowToGenre(ResultSet rs, long rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("GENRE_ID"))
                .name(rs.getString("NAME"))
                .build();
    }

    @Override
    public List<Genre> findAll(){
        String sqlQuery = "SELECT * FROM GENRE";
        return jdbcTemplate.query(sqlQuery, DBGenreRepository::mapRowToGenre);
    }

    @Override
    public Genre getGenre(int id){
        String sqlQuery = "SELECT * FROM GENRE WHERE GENRE_ID = ?";
        List<Genre> genre = jdbcTemplate.query(sqlQuery, DBGenreRepository::mapRowToGenre, id);
        if(genre.size() != 1) {
            return null;
        } else {
            return genre.get(0);
        }
    }
}
