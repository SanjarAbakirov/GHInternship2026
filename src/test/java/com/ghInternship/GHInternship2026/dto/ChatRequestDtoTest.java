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

class ChatRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid ChatRequestDto should have no violations")
    void validDtoShouldHaveNoViolations() {
        ChatRequestDto dto = new ChatRequestDto("Hello AI! How can you assist me?");
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Null message should cause violation")
    void nullMessageShouldCauseViolation() {
        ChatRequestDto dto = new ChatRequestDto(null);
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must not be null"));
    }

    @Test
    @DisplayName("Blank message should cause violation")
    void blankMessageShouldCauseViolation() {
        ChatRequestDto dto = new ChatRequestDto("    ");
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must not be blank"));
    }

    @Test
    @DisplayName("Message exceeding 2000 characters should cause violation")
    void oversizedMessageShouldCauseViolation() {
        String longMessage = "a".repeat(2001);
        ChatRequestDto dto = new ChatRequestDto(longMessage);
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("between 1 and 2000 characters"));
    }
}
