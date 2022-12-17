package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DBUserRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(DBUserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.getUsers();
    }

    private final static Logger log = LoggerFactory.getLogger(UserService.class);

    public Optional<User> getUser(long userId){
        final Optional<User> user = userRepository.getUserById(userId);
        if (user.isEmpty()){
            throw new UserNotFoundException("Пользователя с id " + userId + " не существует");
        }
        return user;
    }

    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    public User putUser(User user) {
        boolean changed = userRepository.updateUser(user);
        if (!changed){
            log.warn("Пользователя с id " + user.getId() + " не существует");
            throw new UserNotFoundException("Пользователя с id " + user.getId() + " не существует");
        }
        return user;
    }

    public void addFriend(Long id, Long friendId){
        if (userRepository.addFriend(id, friendId) == 1){
            log.info("Пользователи " + id + " и " + friendId + " добавлены в друзья");
        } else {
            throw new UserNotFoundException("Как минимум один из id не существует.");
        }
    }

    public void removeFriend(long id, long friendId){
        userRepository.deleteFriend(id, friendId);
    }

    public List<User> showFriends(Long id) {
        return userRepository.getFriendsById(id);
    }

    public List<User> showMutualFriends(Long primaryId, Long secondaryId) {
        return userRepository.getCommonFriends(primaryId, secondaryId);
    }
}
