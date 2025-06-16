package br.com.wslima.javaspringbootrestapi.rest.service;

import br.com.wslima.javaspringbootrestapi.persistence.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    List<User> findAll();
    void deleteById(UUID uuid);

}
