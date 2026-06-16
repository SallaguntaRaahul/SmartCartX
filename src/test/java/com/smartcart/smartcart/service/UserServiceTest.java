package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.model.User;
import com.smartcart.smartcart.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Raahul")
                .lastName("Sallagunta")
                .email("raahul@test.com")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Get my profile - Success")
    void getMyProfile_Success() {
        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));

        UserDTO result = userService
                .getMyProfile("raahul@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail())
                .isEqualTo("raahul@test.com");
        assertThat(result.getFirstName()).isEqualTo("Raahul");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Get my profile - User Not Found")
    void getMyProfile_NotFound() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getMyProfile("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update my profile - Success")
    void updateMyProfile_Success() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Updated", "Name");

        User updatedUser = User.builder()
                .id(1L)
                .firstName("Updated")
                .lastName("Name")
                .email("raahul@test.com")
                .role(User.Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);

        UserDTO result = userService.updateMyProfile(
                "raahul@test.com", request);

        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Get all users - Paginated")
    void getAllUsers_Paginated() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<UserDTO> result = userService.getAllUsers(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail())
                .isEqualTo("raahul@test.com");
    }

    @Test
    @DisplayName("Delete user - Success")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertThatCode(() -> userService.deleteUser(1L))
                .doesNotThrowAnyException();

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete user - Not Found")
    void deleteUser_NotFound() {
        when(userRepository.existsById(999L))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}