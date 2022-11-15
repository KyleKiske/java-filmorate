package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;

@RestController
@RequestMapping("/users")
public class UserController  {

    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    private final HashSet<User> users = new HashSet<>();

    @GetMapping
    public HashSet<User> findAll() {
        return users;
    }

    @PostMapping
    public User create(@RequestBody User user) {

        for (User userFromSet: users){
            if (userFromSet.equals(user)){
                try {
                    throw new UserAlreadyExistException("Данный email уже зарегистрирован");
                } catch (UserAlreadyExistException exception){
                    log.warn(exception.getMessage());
                    return user;
                }
            }
        }

        user = validation(user);
        user.setId(users.size()+1);
        users.add(user);
        log.info("Создан новый пользователь с логином {} под id {}", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public ResponseEntity<User> putUser(@RequestBody User user) {

        for (User userFromList: users){
            if (userFromList.equals(user)){
                userFromList.setLogin(user.getLogin());
                userFromList.setName(user.getName());
                userFromList.setBirthday(user.getBirthday());
                userFromList.setEmail(user.getEmail());
                log.info("Информация о пользователе {} была изменена", userFromList.getId());
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
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

    static class UserAlreadyExistException extends Exception{
        public UserAlreadyExistException(final String message) {
            super(message);
        }
    }

    static class UserNotFoundException extends Exception{
        public UserNotFoundException(final String message) {
            super(message);
        }
    }

    static class UserValidationException extends Exception{
        public UserValidationException(final String message) {
            super(message);
        }
    }

    static class InvalidEmailException  extends Exception{
        public InvalidEmailException (final String message) {
            super(message);
        }
    }


}
