package ru.yandex.practicum.filmorate.service;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage){
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return new ArrayList<>(userStorage.getUsers().values());
    }

    private final static Logger log = LoggerFactory.getLogger(UserService.class);

    public ResponseEntity<User> getUser(long id){
        User user = userStorage.getUsers().get(id);
        if (user == null){
            try {
                throw new UserNotFoundException("Пользователя с id " + id + " не существует");
            } catch (UserNotFoundException exception){
                log.warn(exception.getMessage());
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<User> createUser(User user) {
        if (userStorage.getUsers().containsKey(user.getId())){
            try {
                throw new UserAlreadyExistException("Данный id уже зарегистрирован");
            } catch (UserAlreadyExistException exception){
                log.warn(exception.getMessage());
                return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
            }
        }
        String result = validation(user);
        if (result == null){
            user.setId(userStorage.getUsers().size()+1);
            userStorage.getUsers().put(user.getId(), user);
            log.info("Создан новый пользователь с логином {} под id {}", user.getLogin(), user.getId());
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<User> putUser(User user) {
        if (userStorage.getUsers().containsKey(user.getId())){
            Set<Long> friends = userStorage.getUsers().get(user.getId()).getFriendIds();
            userStorage.getUsers().replace(user.getId(), user);
            userStorage.getUsers().get(user.getId()).setFriendIds(friends);
            log.info("Информация о пользователе {} была изменена", user.getId());
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        log.warn("Пользователя с id " + user.getId() + " не существует");
        return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
    }

    public String addFriend(Long id, Long friendId){
        if (userStorage.getUsers().containsKey(id)){
            if (userStorage.getUsers().containsKey(friendId)){
                userStorage.getUsers().get(id).getFriendIds().add(friendId);
                userStorage.getUsers().get(friendId).getFriendIds().add(id);
            } else {
                    log.warn("пользователя с id " + friendId + " не существует");
                    return friendId.toString();
            }
        } else {
            log.warn("пользователя с id " + id + " не существует");
            return id.toString();
        }
        log.info("Пользователи " + id + " и " + friendId + " добавлены в друзья");
        return "Пользователи " + id + " и " + friendId + " добавлены в друзья";
    }

    @SneakyThrows
    public void removeFriend(Long id, Long friendId) {
        if (userStorage.getUsers().containsKey(id)){
            if (userStorage.getUsers().containsKey(friendId)){
                User first = userStorage.getUsers().get(id);
                User second = userStorage.getUsers().get(friendId);
                Set<Long> mainSet = new HashSet<>();
                Set<Long> friendSet = new HashSet<>();
                if (first.getFriendIds() != null){
                    mainSet = first.getFriendIds();
                }
                if (second.getFriendIds() != null){
                    friendSet = (second.getFriendIds());
                }
                mainSet.remove(friendId);
                friendSet.remove(id);
                first.setFriendIds(mainSet);
                second.setFriendIds(friendSet);
                userStorage.getUsers().replace(id, first);
                userStorage.getUsers().replace(friendId, second);
            } else {
                log.warn("Пользователя с id " + friendId + " не существует");
                throw new UserNotFoundException("Пользователя с id " + friendId + " не существует");
            }
        } else {
            log.warn("Пользователя с id " + id + " не существует");
            throw new UserNotFoundException("Пользователя с id " + id + " не существует");
        }
        log.info("Пользователи " + id + " и " + friendId + " удалены из друзей");
    }

    public ResponseEntity<List<User>> showFriends(Long id) {
        if (userStorage.getUsers().containsKey(id)) {
            Set<Long> friends = userStorage.getUsers().get(id).getFriendIds();
            List<User> result = userStorage.getUsers().values()
                    .stream()
                    .filter(user -> friends.contains(user.getId()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return null;
        }
    }

    public ResponseEntity<List<User>> showMutualFriends(Long primaryId, Long secondaryId) {
        if (userStorage.getUsers().containsKey(primaryId) && userStorage.getUsers().containsKey(secondaryId) ) {
            Set<Long> friends = userStorage.getUsers().get(primaryId).getFriendIds();
            if (friends == null){
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
            }
            Set<Long> intersection = friends.stream()
                    .filter(userStorage.getUsers().get(secondaryId).getFriendIds()::contains)
                    .collect(Collectors.toSet());
            List<User> result = userStorage.getUsers().values()
                    .stream()
                    .filter(user -> intersection.contains(user.getId()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return null;
    }

    public String validation(User user){
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")){
            try {
                throw new UserValidationException("Введен неверный логин");
            } catch (UserValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        } else if ( user.getBirthday().isAfter(LocalDate.now())) {
            try {
                throw new UserValidationException("Введенная дата рождения позже текущего дня");
            } catch (UserValidationException exception){
                log.warn(exception.getMessage());
                return exception.getMessage();
            }
        }

        if (user.getName() == null){
            user.setName(user.getLogin());
        } else if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setFriendIds(new HashSet<>());
        return null;
    }
}
