document.getElementById('registerForm').addEventListener('submit', async function(e) {
    e.preventDefault();  // Останавливаем обычную отправку формы

    // Очищаем предыдущие сообщения
    clearErrors();
    hideMessages();

    // Собираем данные
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Простейшая клиентская валидация
    let hasError = false;
    if (username.length < 3) {
        showError('username', 'Username must be at least 3 characters');
        hasError = true;
    }
    if (!isValidEmail(email)) {
        showError('email', 'Please enter a valid email address');
        hasError = true;
    }
    if (password.length < 6) {
        showError('password', 'Password must be at least 6 characters');
        hasError = true;
    }
    if (password !== confirmPassword) {
        showError('confirmPassword', 'Passwords do not match');
        hasError = true;
    }
    if (hasError) return;

    // Отправляем JSON на сервер
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();

        if (data.success) {
            showSuccess(data.message);
            document.getElementById('registerForm').reset();
        } else {
            showErrorMessage(data.message);
        }
    } catch (error) {
        showErrorMessage('Network error. Please try again.');
    }
});

function showError(fieldId, message) {
    const input = document.getElementById(fieldId);
    const errorSpan = document.getElementById(fieldId + 'Error');
    input.classList.add('error-input');
    errorSpan.textContent = message;
}

function clearErrors() {
    document.querySelectorAll('.error-input').forEach(el => el.classList.remove('error-input'));
    document.querySelectorAll('.error').forEach(el => el.textContent = '');
}

function showSuccess(message) {
    const msgDiv = document.getElementById('message');
    msgDiv.textContent = message;
    msgDiv.style.display = 'block';
    msgDiv.className = 'message';
}

function showErrorMessage(message) {
    const msgDiv = document.getElementById('error-message');
    msgDiv.textContent = message;
    msgDiv.style.display = 'block';
    msgDiv.className = 'error-message';
}

function hideMessages() {
    document.getElementById('message').style.display = 'none';
    document.getElementById('error-message').style.display = 'none';
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}