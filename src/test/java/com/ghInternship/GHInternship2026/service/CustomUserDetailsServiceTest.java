package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты для {@link CustomUserDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should return UserDetails with ROLE_USER when user exists in database")
    void shouldLoadUserByUsername_WhenUserExists() {
        User entity = new User(1L, "alice", "$2a$10$encodedPassword");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(entity));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByUsername("alice");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void shouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");

        verify(userRepository).findByUsername("ghost");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when username is null or blank")
    void shouldThrowUsernameNotFoundException_WhenUsernameIsBlank() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("   "))
                .isInstanceOf(UsernameNotFoundException.class);
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);

        verifyNoInteractions(userRepository);
    }
}
