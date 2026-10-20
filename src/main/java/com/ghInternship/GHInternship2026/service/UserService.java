package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;

import java.util.Optional;

/**
 * Интерфейс сервиса управления пользователями.
 * Инкапсулирует основную бизнес-логику работы с пользователями: регистрация,
 * поиск и проверка подлинности пароля.
 */
public interface UserService {

    /**
     * Регистрация нового пользователя на основе данных из DTO.
     * Пароль хэшируется с использованием PasswordEncoder перед сохранением в БД.
     *
     * @param userData данные для регистрации (имя пользователя и открытый пароль)
     * @return сохранённая сущность {@link User}
     * @throws UserAlreadyExistsException если пользователь с таким именем уже существует
     * @throws IllegalArgumentException если переданные данные некорректны (например, null)
     */
    User registerUser(UserRegistrationDto userData);

    /**
     * Регистрация нового пользователя по имени и открытому паролю.
     * Обеспечивает обратную совместимость с существующим кодом.
     *
     * @param username имя пользователя
     * @param rawPassword открытый пароль
     * @return сохранённая сущность {@link User}
     * @throws UserAlreadyExistsException если логин уже занят
     */
    User registerUser(String username, String rawPassword);

    /**
     * Поиск пользователя по имени пользователя (логину).
     *
     * @param username имя пользователя
     * @return {@link Optional} с пользователем, если найден, иначе {@link Optional#empty()}
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверка соответствия открытого пароля сохранённому хэшу.
     *
     * @param rawPassword открытый пароль
     * @param encodedPassword хэшированный пароль из БД
     * @return {@code true}, если пароли совпадают, иначе {@code false}
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
}
