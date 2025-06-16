package br.com.wslima.javaspringbootrestapi.rest.service.impl;

import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import br.com.wslima.javaspringbootrestapi.rest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUuid( UUID uuid) {
        return userRepository.findByUuid(uuid);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(UUID uuid) {
        userRepository.deleteById(uuid);
    }
}
