package ru.skillfactory.linkshortener.service;

import ru.skillfactory.linkshortener.db.UsersRepository;
import ru.skillfactory.linkshortener.model.User;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public String addUser(String username) {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, username);
        return usersRepository.addUser(user).getId();
    }

    public String getUserByName(String username) {
        Optional<User> userOptional = usersRepository.getUserByName(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            return addUser(username);
        }
    }
}
