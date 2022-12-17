package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.repository.MPARepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class DBMPARepository implements MPARepository {

    private final JdbcTemplate jdbcTemplate;

    public DBMPARepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static MPA mapRowToMPA(ResultSet rs, long rowNum) throws SQLException {
        return MPA.builder()
                .id(rs.getInt("MPA_ID"))
                .name(rs.getString("MPA_NAME"))
                .build();
    }

    @Override
    public List<MPA> findAll(){
        String sqlQuery = "SELECT * FROM MPA";
        return jdbcTemplate.query(sqlQuery, DBMPARepository::mapRowToMPA);
    }

    @Override
    public MPA getMPA(int id){
        String sqlQuery = "SELECT * FROM MPA WHERE MPA_ID = ?";
        List<MPA> mpa = jdbcTemplate.query(sqlQuery, DBMPARepository::mapRowToMPA, id);
        if(mpa.size() != 1) {
            return null;
        } else {
            return mpa.get(0);
        }
    }

}
