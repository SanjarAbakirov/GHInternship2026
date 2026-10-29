package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;
import com.ghInternship.GHInternship2026.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Веб-контроллер для обработки регистрации пользователей по паттерну Post/Redirect/Get (PRG)
 * с использованием {@link RedirectAttributes} (flash attributes).
 * Позволяет передавать одноразовые flash-сообщения между редиректами без риска повторной отправки формы.
 */
@Controller
public class RegistrationWebController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationWebController.class);

    private final UserService userService;

    public RegistrationWebController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Обработка отправки формы регистрации.
     * При успешной регистрации сохраняет flash-атрибуты через {@link RedirectAttributes}
     * и выполняет редирект на эндпоинт /registration-success (паттерн Post/Redirect/Get).
     * При ошибках валидации возвращает пользователя на /register с сохранением flash-сообщений.
     *
     * @param registrationDto    DTO с учетными данными пользователя
     * @param bindingResult      результаты серверной валидации Bean Validation
     * @param redirectAttributes атрибуты перенаправления для сохранения flash-сообщений в сессии
     * @return строка редиректа на страницу успеха или страницу регистрации при ошибке
     */
    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE, "*/*"})
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        log.info("Received web registration request for username: {}",
                registrationDto != null ? registrationDto.username() : "null");

        if (bindingResult.hasErrors()) {
            log.warn("Web registration validation failed with {} error(s)", bindingResult.getErrorCount());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", registrationDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка валидации полей формы регистрации");
            return "redirect:/register";
        }

        try {
            User createdUser = userService.registerUser(registrationDto);
            log.info("User '{}' registered successfully. Setting flash attributes in RedirectAttributes", createdUser.getUsername());

            // Сохранение flash-атрибутов: автоматически попадают во FlashMap и будут удалены после первого чтения
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация прошла успешно! Добро пожаловать, " + createdUser.getUsername() + "!");
            redirectAttributes.addFlashAttribute("username", createdUser.getUsername());

            return "redirect:/registration-success";
        } catch (UserAlreadyExistsException ex) {
            log.warn("Web registration conflict: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("user", registrationDto);
            return "redirect:/register";
        }
    }

    /**
     * Страница/эндпоинт успеха после перенаправления (Post/Redirect/Get).
     * Автоматически принимает flash-атрибуты 'successMessage' и 'username' из сессии Spring MVC.
     * Возвращает HTML-страницу для браузера либо JSON-ответ при запросах API / MockMvc.
     *
     * @param successMessage flash-сообщение об успехе
     * @param username       имя зарегистрированного пользователя
     * @param request        HTTP-запрос
     * @return ответ со статусом 200 OK (HTML или JSON)
     */
    @GetMapping("/registration-success")
    @ResponseBody
    public ResponseEntity<?> showSuccessPage(
            @ModelAttribute("successMessage") String successMessage,
            @ModelAttribute("username") String username,
            HttpServletRequest request
    ) {
        log.info("Displaying registration success page. Flash message: '{}', user: '{}'", successMessage, username);

        String effectiveUser = (username != null && !username.isBlank()) ? username : "Новый пользователь";
        String effectiveMessage = (successMessage != null && !successMessage.isBlank())
                ? successMessage
                : "Регистрация прошла успешно! Теперь вы можете войти в систему.";

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            String html = generateSuccessHtml(effectiveUser, effectiveMessage);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                    .body(html);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("status", "SUCCESS");
        data.put("username", effectiveUser);
        data.put("successMessage", effectiveMessage);
        return ResponseEntity.ok(data);
    }

    /**
     * Генерирует HTML-представление страницы успеха в стилистике Dark Glassmorphism.
     */
    private String generateSuccessHtml(String username, String message) {
        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Регистрация успешна — GHInternship 2026</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700&display=swap" rel="stylesheet">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: 'Inter', -apple-system, sans-serif;
                        background: radial-gradient(circle at top left, #1e1b4b, #0f172a 60%, #030712);
                        color: #f8fafc;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 1.5rem;
                    }
                    .card {
                        background: rgba(30, 41, 59, 0.7);
                        backdrop-filter: blur(16px);
                        -webkit-backdrop-filter: blur(16px);
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 35px rgba(16, 185, 129, 0.2);
                        border-radius: 20px;
                        max-width: 480px;
                        width: 100%;
                        padding: 2.5rem;
                        text-align: center;
                    }
                    .icon-wrap {
                        width: 72px;
                        height: 72px;
                        margin: 0 auto 1.5rem;
                        border-radius: 50%;
                        background: rgba(16, 185, 129, 0.15);
                        border: 2px solid #10b981;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        box-shadow: 0 0 25px rgba(16, 185, 129, 0.35);
                    }
                    .icon-wrap svg {
                        width: 36px;
                        height: 36px;
                        stroke: #10b981;
                        stroke-width: 2.5;
                        fill: none;
                    }
                    h1 {
                        font-family: 'Outfit', sans-serif;
                        font-size: 1.75rem;
                        font-weight: 700;
                        color: #f1f5f9;
                        margin-bottom: 0.75rem;
                    }
                    .user-badge {
                        display: inline-block;
                        padding: 0.35rem 0.85rem;
                        background: rgba(99, 102, 241, 0.15);
                        border: 1px solid rgba(99, 102, 241, 0.3);
                        color: #a5b4fc;
                        border-radius: 9999px;
                        font-size: 0.875rem;
                        font-weight: 600;
                        margin-bottom: 1.25rem;
                    }
                    .message {
                        color: #94a3b8;
                        font-size: 0.95rem;
                        line-height: 1.6;
                        margin-bottom: 2rem;
                    }
                    .btn {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        width: 100%;
                        padding: 0.85rem 1.5rem;
                        background: linear-gradient(135deg, #6366f1, #4f46e5);
                        color: #ffffff;
                        font-size: 1rem;
                        font-weight: 600;
                        text-decoration: none;
                        border-radius: 12px;
                        box-shadow: 0 4px 15px rgba(99, 102, 241, 0.35);
                        transition: all 0.2s ease;
                    }
                    .btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon-wrap">
                        <svg viewBox="0 0 24 24"><path d="M5 13l4 4L19 7"/></svg>
                    </div>
                    <h1>Регистрация успешна!</h1>
                    <div class="user-badge">{{USERNAME}}</div>
                    <p class="message">{{MESSAGE}}</p>
                    <a href="http://localhost:5173" class="btn">Перейти в приложение React</a>
                </div>
            </body>
            </html>
            """
            .replace("{{USERNAME}}", username)
            .replace("{{MESSAGE}}", message);
    }
}
