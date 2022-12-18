package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class DBUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final ValidationService validationService;

    public DBUserRepository(JdbcTemplate jdbcTemplate, ValidationService validationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.validationService = validationService;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        final String sqlQuery = "select USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY from USERS where USER_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()){
            User user = new User(
                    userRows.getLong("USER_ID"),
                    userRows.getString("EMAIL"),
                    userRows.getString("LOGIN"),
                    userRows.getString("NAME"),
                    Objects.requireNonNull(userRows.getDate("BIRTHDAY")).toLocalDate());

            log.info("Найден пользователь: {} ", user.getId());
            return Optional.of(user);
        } else {
            log.info("Ничего не найдено");
            return Optional.empty();
        }
    }

    private static Optional<User> mapRowToUser(ResultSet rs, long rowNum) throws SQLException {
        return Optional.ofNullable(User.builder()
                .id(rs.getLong("USER_ID"))
                .email(rs.getString("EMAIL"))
                .login(rs.getString("LOGIN"))
                .name(rs.getString("NAME"))
                .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                .build());
    }
    @Override
    public User createUser(User user) {
        User validatedUser = validationService.validateUser(user);
        String sqlQuery = "insert into USERS (EMAIL, LOGIN, NAME, BIRTHDAY) values (?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery, new String[]{"USER_ID"});
            stmt.setString(1, validatedUser.getEmail());
            stmt.setString(2, validatedUser.getLogin());
            stmt.setString(3, validatedUser.getName());
            final LocalDate birthday = validatedUser.getBirthday();
            if (birthday == null){
                stmt.setNull(4, Types.DATE);
            } else {
                stmt.setDate(4, Date.valueOf(birthday));
            }
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public List<Optional<User>> getUsers() {
        final String sqlQuery = "select USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY from USERS";
        return jdbcTemplate.query(sqlQuery, DBUserRepository::mapRowToUser);
    }

    @Override
    public boolean deleteUser(long id) {
        final String sqlQuery = "delete from USERS where USER_ID = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        String sqlQuery = "update USERS " +
                "set EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                "where USER_ID = ?";

        return jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()) > 0;
    }

    @Override
    public int addFriend(Long id, Long friendId) {
        final String sqlQuery = "insert into FRIEND (FRIEND_1, FRIEND_2) values (?,?)";
        try {
            jdbcTemplate.update(sqlQuery, id, friendId);
        } catch (DataAccessException e){
            return 0;
        }
        return 1;
    }

    @Override
    public List<Optional<User>> getFriendsById(Long id) {
        final String sqlQuery =
                "SELECT u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY " +
                        "FROM FRIEND AS f " +
                        "JOIN USERS AS u ON u.USER_ID = f.FRIEND_2 " +
                        "WHERE f.FRIEND_1 = ? AND f.Confirmed = TRUE ";
        return jdbcTemplate.query(sqlQuery, DBUserRepository::mapRowToUser, id);
    }

    @Override
    public List<Optional<User>> getCommonFriends(Long primaryId, Long secondaryId) {
        final String sqlQuery =
                "SELECT u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY FROM FRIEND f " +
                    "join USERS u on f.FRIEND_2 = u.USER_ID " +
                    "where f.FRIEND_1 IN (?,?) " +
                    "GROUP BY u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY " +
                    "HAVING COUNT(u.USER_ID) >= 2 " +
            "UNION " +
                "SELECT u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY FROM FRIEND f " +
                    "join USERS u on f.FRIEND_1 = u.USER_ID " +
                    "where f.FRIEND_2 IN (?,?) " +
                    "GROUP BY u.USER_ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY " +
                    "HAVING COUNT(u.USER_ID) >= 2";

        return jdbcTemplate.query(sqlQuery, DBUserRepository::mapRowToUser, primaryId, secondaryId, primaryId, secondaryId);
    }

    @Override
    public void deleteFriend(long primaryId, long secondaryId) {
        final String sqlQuery =
                "DELETE FROM FRIEND WHERE FRIEND_1 = ? AND FRIEND_2 = ?";
        jdbcTemplate.update(sqlQuery, primaryId, secondaryId);
    }
}
