package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA репозиторий для сущности {@link User}.
 *
 * <p>Наследует {@link JpaRepository}, передавая:
 * <ul>
 *   <li>{@code User} — тип управляемой JPA-сущности;</li>
 *   <li>{@code Long} — тип первичного ключа (Primary Key / {@code @Id}).</li>
 * </ul>
 *
 * Все базовые CRUD-операции (save, findById, findAll, delete и др.)
 * генерируются Spring Data JPA автоматически во время выполнения (at runtime).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по уникальному имени пользователя (логину).
     *
     * @param username имя пользователя
     * @return {@link Optional} с найденным пользователем либо {@code Optional.empty()}
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверка существования пользователя с указанным логином.
     * Полезно для валидации перед регистрацией новых пользователей.
     *
     * @param username имя пользователя
     * @return {@code true}, если пользователь существует, иначе {@code false}
     */
    boolean existsByUsername(String username);
}
