const API_URL = 'http://localhost:8080/api/v1'; 
const USE_MOCK = true;

const form = {
    emailLabel: document.getElementById('email'),
    passwordLabel: document.getElementById('password'),
    button: document.querySelector('.button.primary'),
    error: document.querySelector('.input-error')
};

const emailInput = form.emailLabel.querySelector('input');
const passwordInput = form.passwordLabel.querySelector('input');

if (sessionStorage.getItem('regSuccess')) {
    const successMsg = document.getElementById('regSuccessMsg');
    if (successMsg) {
        successMsg.textContent = 'Account created. Log in.';
        successMsg.style.color = 'green';
        successMsg.style.display = 'block';
        sessionStorage.removeItem('regSuccess');
    }
}

function checkForm() {
    if (emailInput.value && passwordInput.value) {
        form.button.classList.remove('disable');
    } else {
        form.button.classList.add('disable');
    }
}

function handleInput(e, name) {
    const { value } = e.target;
    const labelElement = name === 'email' ? form.emailLabel : form.passwordLabel;
    
    if (value) {
        labelElement.classList.add('filled');
    } else {
        labelElement.classList.remove('filled');
    }
    checkForm();
}

async function handleLogin() {
    const email = emailInput.value;
    const password = passwordInput.value;

    const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!reg.test(email)) {
        showError('invalid email');
        return;
    }

    form.button.classList.add('disable');
    form.button.textContent = 'entry...';
    hideError();

    try {
        if (USE_MOCK) {
            await new Promise(resolve => setTimeout(resolve, 800)); // Задержка 0.8 сек
            
            // Имитация успеха
            sessionStorage.setItem('authToken', 'mock-token-12345');
            window.location.href = '/dashboard.html'; 
        } else {
            const response = await fetch(`${API_URL}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
            const data = await response.json();
            
            sessionStorage.setItem('authToken', data.token);
            window.location.href = '/dashboard.html';
        }

    } catch (error) {
        console.error(error);
        showError(error.message || 'invalid login or password');
        form.button.classList.remove('disable');
        form.button.textContent = 'Log in';
    }
}

function showError(msg) {
    form.error.textContent = msg;
    form.error.classList.add('view');
    form.emailLabel.classList.add('error');
}

function hideError() {
    form.error.classList.remove('view');
    form.emailLabel.classList.remove('error');
}

emailInput.oninput = (e) => handleInput(e, 'email');
passwordInput.oninput = (e) => handleInput(e, 'password');

document.querySelector('form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleLogin();
});
