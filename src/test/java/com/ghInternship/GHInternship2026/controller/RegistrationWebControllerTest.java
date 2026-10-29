package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;
import com.ghInternship.GHInternship2026.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для {@link RegistrationWebController}, проверяющие реализацию паттерна Post/Redirect/Get
 * и корректную установку flash-атрибутов через Spring's {@link org.springframework.web.servlet.mvc.support.RedirectAttributes}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /register should redirect to /registration-success and set flash attributes upon successful registration")
    void register_ShouldRedirectToSuccess_AndSetFlashAttributes_WhenValid() throws Exception {
        User createdUser = new User();
        createdUser.setId(10L);
        createdUser.setUsername("alex_smith");
        createdUser.setPasswordHash("$2a$10$dummyHashValue");

        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "alex_smith")
                        .param("password", "SecurePassword123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration-success"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", containsString("Регистрация прошла успешно")))
                .andExpect(flash().attribute("username", "alex_smith"));

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("POST /register should redirect back to /register with error flash attributes when validation fails")
    void register_ShouldRedirectBack_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "ab") // меньше 3 символов
                        .param("password", "123")) // меньше 6 символов
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.user"))
                .andExpect(flash().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /register should redirect back to /register when user already exists")
    void register_ShouldRedirectBack_WhenUserAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new UserAlreadyExistsException("Пользователь с именем 'alex_smith' уже существует"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "alex_smith")
                        .param("password", "SecurePassword123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", containsString("уже существует")))
                .andExpect(flash().attributeExists("user"));
    }

    @Test
    @DisplayName("GET /registration-success should render HTML with flash attributes when Accept is text/html")
    void showSuccessPage_ShouldReturnHtml_WhenAcceptIsTextHtml() throws Exception {
        mockMvc.perform(get("/registration-success")
                        .header("Accept", MediaType.TEXT_HTML_VALUE)
                        .characterEncoding("UTF-8")
                        .flashAttr("successMessage", "Добро пожаловать в систему!")
                        .flashAttr("username", "alex_smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Регистрация успешна!")))
                .andExpect(content().string(containsString("alex_smith")))
                .andExpect(content().string(containsString("Добро пожаловать в систему!")));
    }

    @Test
    @DisplayName("GET /registration-success should return JSON with flash attributes when called via API")
    void showSuccessPage_ShouldReturnJson_WhenCalledViaApi() throws Exception {
        mockMvc.perform(get("/registration-success")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .flashAttr("successMessage", "Registration OK")
                        .flashAttr("username", "alex_smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.username").value("alex_smith"))
                .andExpect(jsonPath("$.successMessage").value("Registration OK"));
    }
}
