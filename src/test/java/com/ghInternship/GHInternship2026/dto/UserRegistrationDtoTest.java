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
    @DisplayName("Blank username should violate validation")
    void blankUsernameShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("  ", "SecurePassword123!");
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
    @DisplayName("Blank password should violate validation")
    void blankPasswordShouldFail() {
        UserRegistrationDto dto = new UserRegistrationDto("validUser", "");
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
}
