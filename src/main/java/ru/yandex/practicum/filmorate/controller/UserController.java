package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@RequestBody User user) {

        if (users.containsKey(user.getId())){
            try {
                throw new UserAlreadyExistException("Данный email уже зарегистрирован");
            } catch (UserAlreadyExistException exception){
                log.warn(exception.getMessage());
                return user;
            }
        }

        user = validation(user);
        user.setId(users.size()+1);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь с логином {} под id {}", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public ResponseEntity<User> putUser(@RequestBody User user) {

        if (users.containsKey(user.getId())){
            users.replace(user.getId(), user);
            log.info("Информация о пользователе {} была изменена", user.getId());
            return new ResponseEntity<>(user, HttpStatus.OK);
        }

        try {
            throw new UserNotFoundException("Пользователя с id " + user.getId() + " не существует");
        } catch (UserNotFoundException exception){
            log.warn(exception.getMessage());
            return new ResponseEntity<>(user, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public User validation(User user){
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")){
            try {
                throw new UserValidationException("Введен неверный логин");
            } catch (UserValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        } else if ( user.getBirthday().isAfter(LocalDate.now())) {
            try {
                throw new UserValidationException("Введенная дата рождения позже текущего дня");
            } catch (UserValidationException exception){
                log.warn(exception.getMessage());
                return null;
            }
        }

        if (user.getName() == null){
            user.setName(user.getLogin());
        } else if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

}
