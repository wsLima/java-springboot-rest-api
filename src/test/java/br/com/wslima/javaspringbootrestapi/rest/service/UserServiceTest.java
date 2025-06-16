package br.com.wslima.javaspringbootrestapi.rest.service;

import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import br.com.wslima.javaspringbootrestapi.rest.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldSaveUserSuccessfully() {
        // Arrange
        User user = new User();
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        // Act
        User saved = userService.save(user);

        // Assert
        assertNotNull(saved);
    }
}

