package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> getUserById(Long id);

    User createUser(User user);

    List<Optional<User>> getUsers();

    boolean deleteUser(long id);

    boolean updateUser(User user);

    int addFriend(Long id, Long friendId);

    List<Optional<User>> getFriendsById(Long id);

    List<Optional<User>> getCommonFriends(Long primaryId, Long secondaryId);

    void deleteFriend(long primaryId, long secondaryId);
}
