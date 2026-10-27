package com.ghInternship.GHInternship2026.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegistrationDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid UserRegistrationDto should have no violations")
    void validDtoShouldPassValidation() {
        UserRegistrationDto dto = new UserRegistrationDto("john_doe", "SecurePassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Empty string username should violate validation")
    void emptyUsernameShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("", "SecurePassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Blank whitespace username should violate validation")
    void blankUsernameShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("    ", "SecurePassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Null username should violate validation")
    void nullUsernameShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto(null, "SecurePassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Short username (< 3 chars) should violate validation")
    void shortUsernameShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("ab", "SecurePassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Empty string password should violate validation")
    void emptyPasswordShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("validUser", "");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Blank whitespace password should violate validation")
    void blankPasswordShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("validUser", "     ");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Null password should violate validation")
    void nullPasswordShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("validUser", null);
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Short password (< 6 chars) should violate validation")
    void shortPasswordShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("validUser", "12345");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Both fields empty should violate validation on both properties")
    void bothFieldsEmptyShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("", "");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Username exceeding 50 chars should violate @Size validation")
    void usernameExceeding50CharsShouldFail() {
        String longUsername = "a".repeat(51);
        UserRegistrationDto dto = new UserRegistrationDto(longUsername, "ValidPassword123!");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username")
                && v.getMessage().contains("between 3 and 50 characters"));
    }

    @Test
    @DisplayName("Password exceeding 100 chars should violate @Size validation")
    void passwordExceeding100CharsShouldFail() {
        String longPassword = "p".repeat(101);
        UserRegistrationDto dto = new UserRegistrationDto("validUser", longPassword);
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password")
                && v.getMessage().contains("between 6 and 100 characters"));
    }

    @Test
    @DisplayName("Boundary valid lengths (3 chars for username, 6 chars for password) should pass")
    void boundaryMinValidLengthsShouldPass() {
        UserRegistrationDto dto = new UserRegistrationDto("abc", "123456");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Boundary valid max lengths (50 chars for username, 100 chars for password) should pass")
    void boundaryMaxValidLengthsShouldPass() {
        UserRegistrationDto dto = new UserRegistrationDto("u".repeat(50), "p".repeat(100));
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
