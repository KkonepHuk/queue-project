import { Api } from './js/api.js';

const form = {
    emailLabel: document.getElementById('email'),
    passwordLabel: document.getElementById('password'),
    button: document.querySelector('.button.primary'),
    error: document.getElementById('loginError')
};

const emailInput = form.emailLabel.querySelector('input');
const passwordInput = form.passwordLabel.querySelector('input');

if (sessionStorage.getItem('regSuccess')) {
    const successMsg = document.getElementById('regSuccessMsg');
    if (successMsg) {
        successMsg.textContent = 'Account created. Log in.';
        successMsg.classList.add('view');
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

    try {
        const response = await Api.login({ email, password });
        
        sessionStorage.setItem('authToken', response.token);
        window.location.href = '/dashboard';

    } catch (error) {
        showError(error.message || 'Login failed');
        form.button.classList.remove('disable');
        form.button.textContent = 'Log in';
    }
}

function showError(msg) {
    if (!form.error) return;
    form.error.textContent = msg;
    form.error.classList.add('view');
    form.emailLabel.classList.add('error');
}

function hideError() {
    if (!form.error) return;
    form.error.classList.remove('view');
    form.emailLabel.classList.remove('error');
}

emailInput.oninput = (e) => handleInput(e, 'email');
passwordInput.oninput = (e) => handleInput(e, 'password');

document.querySelector('form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleLogin();
});
