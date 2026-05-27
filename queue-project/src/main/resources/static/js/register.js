const API_URL = '/api/v1';
const USE_MOCK = false;

const form = document.getElementById('regForm');
const btn = form.querySelector('.button');
const errorEl = form.querySelector('.input-error');

const inputs = {
    firstName: form.querySelector('#firstName input'),
    lastName: form.querySelector('#lastName input'),
    email: form.querySelector('#email input'),
    password: form.querySelector('#password input'),
    confirm: form.querySelector('#confirmPassword input')
};

//плавающие лейблы + активация кнопки
Object.values(inputs).forEach(input => {
    const updateField = () => {
        input.closest('label').classList.toggle('filled', input.value.trim() !== '');
        checkForm();
        hideError();
    };

    updateField(); // Проверка при загрузке (автозаполнение)
    input.addEventListener('input', updateField); // При вводе
    input.addEventListener('blur', updateField);  // При уходе с поля
    input.addEventListener('change', updateField); // Для мобильных/вставок
});

function checkForm() {
    const allFilled = Object.values(inputs).every(inp => inp.value.trim() !== '');
    btn.classList.toggle('disable', !allFilled);
}

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.add('view');
}

function hideError() {
    errorEl.classList.remove('view');
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError();

    const email = inputs.email.value.trim();
    const pass = inputs.password.value.trim();
    const confirm = inputs.confirm.value.trim();
    const firstName = inputs.firstName.value.trim();
    const lastName = inputs.lastName.value.trim();

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return showError('Invalid email format');
    if (pass.length < 8) return showError('Password must be at least 8 characters');
    if (pass !== confirm) return showError('Passwords do not match');

    btn.classList.add('disable');
    btn.textContent = 'Registering...';

    const payload = { email, password: pass, firstName, lastName };

    try {
        if (USE_MOCK) {
            await new Promise(r => setTimeout(r, 800));
        } else {
            const res = await fetch(`${API_URL}/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                if (res.status === 409) throw new Error('Email already registered');
                if (res.status === 400) throw new Error(errData.message || 'Invalid data');
                throw new Error(`Server error: ${res.status}`);
            }
        }

        // успех -> флаг для логина -> редирект
        sessionStorage.setItem('regSuccess', 'true');
        window.location.href = '/index.html';
    } catch (error) {
        console.error(error);
        showError(error.message || 'Registration failed');
        btn.classList.remove('disable');
        btn.textContent = 'Register';
    }
});
