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

class UserLoginDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid UserLoginDto should pass validation")
    void validDtoShouldPass() {
        UserLoginDto dto = new UserLoginDto("validUser", "secret123");
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Blank username in UserLoginDto should fail validation")
    void blankUsernameShouldFail() {
        UserLoginDto dto = new UserLoginDto("  ", "secret123");
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Blank password in UserLoginDto should fail validation")
    void blankPasswordShouldFail() {
        UserLoginDto dto = new UserLoginDto("validUser", "");
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
