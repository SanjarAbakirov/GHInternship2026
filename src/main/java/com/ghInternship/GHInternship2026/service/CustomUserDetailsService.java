package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Реализация {@link UserDetailsService} для загрузки учетных записей пользователей из БД {@link UserRepository}
 * при проверке JWT-токенов в цепочке фильтров Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            log.warn("Попытка загрузки UserDetails с пустым именем пользователя");
            throw new UsernameNotFoundException("Username must not be blank");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь '{}' не найден при загрузке UserDetails", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("Успешно загружен UserDetails для пользователя '{}'", user.getUsername());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
