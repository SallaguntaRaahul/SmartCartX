package com.smartcart.smartcart.repository;

import com.smartcart.smartcart.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder()
                        .firstName("Raahul")
                        .lastName("Sallagunta")
                        .email("raahul@test.com")
                        .password("encodedPassword")
                        .role(User.Role.CUSTOMER)
                        .build());
    }

    @Test
    @DisplayName("Find by email - found")
    void findByEmail_Found() {
        Optional<User> result = userRepository
                .findByEmail("raahul@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName())
                .isEqualTo("Raahul");
        assertThat(result.get().getRole())
                .isEqualTo(User.Role.CUSTOMER);
    }

    @Test
    @DisplayName("Find by email - not found")
    void findByEmail_NotFound() {
        Optional<User> result = userRepository
                .findByEmail("unknown@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Exists by email - true")
    void existsByEmail_True() {
        assertThat(userRepository
                .existsByEmail("raahul@test.com"))
                .isTrue();
    }

    @Test
    @DisplayName("Exists by email - false")
    void existsByEmail_False() {
        assertThat(userRepository
                .existsByEmail("unknown@test.com"))
                .isFalse();
    }

    @Test
    @DisplayName("Save user - success")
    void saveUser_Success() {
        User saved = userRepository.save(
                User.builder()
                        .firstName("Alice")
                        .lastName("Smith")
                        .email("alice@test.com")
                        .password("encoded")
                        .role(User.Role.CUSTOMER)
                        .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail())
                .isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("Delete user - success")
    void deleteUser_Success() {
        userRepository.deleteById(testUser.getId());

        assertThat(userRepository
                .findByEmail("raahul@test.com"))
                .isEmpty();
    }
}