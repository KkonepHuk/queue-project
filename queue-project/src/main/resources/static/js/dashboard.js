const API_URL = '/api/v1';
const USE_MOCK = false;

const token = sessionStorage.getItem('authToken');
if (!token) {
    window.location.href = '/index.html';
}

const content = document.getElementById('content');
const pageTitle = document.getElementById('pageTitle');
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const navItems = document.querySelectorAll('.nav-item');
let currentUser = null;

function authHeaders(extraHeaders = {}) {
    return {
        ...extraHeaders,
        'Authorization': `Bearer ${token}`
    };
}

function redirectToLogin() {
    sessionStorage.removeItem('authToken');
    window.location.href = '/index.html';
}

async function parseJsonResponse(res, fallbackMessage) {
    if (res.status === 401 || res.status === 403) {
        redirectToLogin();
        throw new Error('Session expired');
    }

    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || fallbackMessage || `Request failed: ${res.status}`);
    }

    return res.json();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function formatDate(value) {
    if (!value) return '-';
    return new Date(value).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

async function loadUserProfile() {
    const avatarEl = document.querySelector('.avatar');
    let firstName = 'User';
    let lastName = '';

    if (USE_MOCK) {
        firstName = 'John';
        lastName = 'Doe';
        userName.textContent = `${firstName} ${lastName}`;
        avatarEl.textContent = 'JD';
        return;
    }
    
    try {
        const res = await fetch(`${API_URL}/users/me`, {
            headers: authHeaders()
        });
        const user = await parseJsonResponse(res, 'Failed to load profile');
        currentUser = user;
        firstName = user.firstName || 'User';
        lastName = user.lastName || '';
        userName.textContent = `${firstName} ${lastName}`.trim();
        const initials = (firstName[0] + (lastName[0] || '')).toUpperCase();
        avatarEl.textContent = initials;
    } catch (err) {
        console.error('Failed to load profile:', err);
        avatarEl.textContent = 'U';
    }
}

async function refreshProfile() {
    await loadUserProfile();
    if (!currentUser) {
        throw new Error('Failed to load profile');
    }
    return currentUser;
}

function navigateTo(view, params = {}) {
    // Обновляем активный пункт меню
    navItems.forEach(item => {
        item.classList.toggle('active', item.dataset.view === view);
    });
    
    // Рендерим контент
    switch(view) {
        case 'my-groups':
            renderMyGroups();
            break;
        case 'group-detail':
            renderGroupDetail(params.groupId);
            break;
        case 'browse-groups':
            renderBrowseGroups();
            break;
        case 'notifications':
            renderNotifications();
            break;
        case 'profile':
            renderProfile();
            break;
    }
}

async function renderMyGroups() {
    pageTitle.textContent = 'My Groups';
    
    if (USE_MOCK) {
        content.innerHTML = `
            <div class="groups-grid">
                <div class="group-card" onclick="navigateTo('group-detail', {groupId: '1'})">
                    <span class="group-status open">Open</span>
                    <h3 class="group-name">Computer Science 101</h3>
                    <p class="group-description">Intro to Programming - Spring 2026</p>
                    <div class="group-stats">
                        <span>👥 45 members</span>
                        <span>📅 3 active events</span>
                    </div>
                </div>
                <div class="group-card" onclick="navigateTo('group-detail', {groupId: '2'})">
                    <span class="group-status closed">Closed</span>
                    <h3 class="group-name">Data Structures</h3>
                    <p class="group-description">Advanced algorithms and data structures</p>
                    <div class="group-stats">
                        <span>👥 32 members</span>
                        <span>📅 0 active events</span>
                    </div>
                </div>
            </div>
        `;
        return;
    }
    
    try {
        const res = await fetch(`${API_URL}/groups`, {
            headers: authHeaders()
        });
        const groups = await parseJsonResponse(res, 'Failed to load groups');

        if (groups.length === 0) {
            content.innerHTML = `
                <div class="page-actions">
                    <button class="create-btn" id="showCreateGroupBtn">Create Group</button>
                </div>
                <form class="panel-form hidden" id="createGroupForm">
                    <label>
                        <span>Name</span>
                        <input name="name" type="text" required>
                    </label>
                    <label>
                        <span>Description</span>
                        <textarea name="description" rows="3"></textarea>
                    </label>
                    <div class="form-row">
                        <button type="submit" class="create-btn">Save Group</button>
                        <button type="button" class="secondary-btn" id="cancelCreateGroupBtn">Cancel</button>
                    </div>
                    <div class="form-message" id="createGroupMessage"></div>
                </form>
                <div class="placeholder">
                    <h3>No groups yet</h3>
                    <p>Create or join a group to start working with queues.</p>
                </div>
            `;
            bindCreateGroupControls();
            return;
        }
        
        content.innerHTML = `
            <div class="page-actions">
                <button class="create-btn" id="showCreateGroupBtn">Create Group</button>
            </div>
            <form class="panel-form hidden" id="createGroupForm">
                <label>
                    <span>Name</span>
                    <input name="name" type="text" required>
                </label>
                <label>
                    <span>Description</span>
                    <textarea name="description" rows="3"></textarea>
                </label>
                <div class="form-row">
                    <button type="submit" class="create-btn">Save Group</button>
                    <button type="button" class="secondary-btn" id="cancelCreateGroupBtn">Cancel</button>
                </div>
                <div class="form-message" id="createGroupMessage"></div>
            </form>
            <div class="groups-grid">
                ${groups.map(group => `
                    <div class="group-card" onclick="navigateTo('group-detail', {groupId: '${group.groupId}'})">
                        <span class="group-status open">Open</span>
                        <h3 class="group-name">${escapeHtml(group.name)}</h3>
                        <p class="group-description">${escapeHtml(group.description || 'No description')}</p>
                        <div class="group-stats">
                            <span>Created ${formatDate(group.createdAt)}</span>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
        bindCreateGroupControls();
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load groups</div>';
    }
}

function bindCreateGroupControls() {
    const showBtn = document.getElementById('showCreateGroupBtn');
    const cancelBtn = document.getElementById('cancelCreateGroupBtn');
    const form = document.getElementById('createGroupForm');
    const message = document.getElementById('createGroupMessage');

    if (!showBtn || !form) return;

    showBtn.addEventListener('click', () => {
        form.classList.remove('hidden');
        showBtn.classList.add('hidden');
        form.querySelector('input[name="name"]').focus();
    });

    cancelBtn?.addEventListener('click', () => {
        form.reset();
        form.classList.add('hidden');
        showBtn.classList.remove('hidden');
        message.textContent = '';
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        message.textContent = '';

        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;

        try {
            const payload = {
                name: form.elements.name.value.trim(),
                description: form.elements.description.value.trim()
            };

            const res = await fetch(`${API_URL}/groups`, {
                method: 'POST',
                headers: authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(payload)
            });
            await parseJsonResponse(res, 'Failed to create group');
            await renderMyGroups();
        } catch (err) {
            message.textContent = err.message || 'Failed to create group';
        } finally {
            submitBtn.disabled = false;
        }
    });
}

async function renderGroupDetail(groupId) {
    pageTitle.textContent = 'Group Details';
    
    if (USE_MOCK) {
        content.innerHTML = `
            <div class="group-header">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <h2 class="group-title">Computer Science 101</h2>
                        <p class="group-subtitle">Intro to Programming - Spring 2026</p>
                    </div>
                    <button class="create-btn" onclick="alert('Create Event modal')">
                        + Create Event
                    </button>
                </div>
            </div>
            
            <div class="events-table">
                <div class="events-header">
                    <h3>Events</h3>
                    <p>Manage queue events and view participant lists</p>
                </div>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Event Name</th>
                            <th>Date</th>
                            <th>Queue Status</th>
                            <th>Participants</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Office Hours - Week 12</td>
                            <td>Mar 25, 2026</td>
                            <td><span class="status-badge open">Open</span></td>
                            <td>12 / 20</td>
                            <td class="action-icons">
                                <button title="Edit">✏️</button>
                                <button title="Lock">🔒</button>
                                <button title="Download">⬇️</button>
                            </td>
                        </tr>
                        <tr>
                            <td>Lab Session 1</td>
                            <td>Mar 26, 2026</td>
                            <td><span class="status-badge open">Open</span></td>
                            <td>8 / 15</td>
                            <td class="action-icons">
                                <button title="Edit">✏️</button>
                                <button title="Lock">🔒</button>
                                <button title="Download">⬇️</button>
                            </td>
                        </tr>
                        <tr>
                            <td>Project Review Session</td>
                            <td>Mar 27, 2026</td>
                            <td><span class="status-badge waiting">Waiting</span></td>
                            <td>0 / 10</td>
                            <td class="action-icons">
                                <button title="Edit">✏️</button>
                                <button title="Download">⬇️</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        `;
        return;
    }
    
    try {
        const [groupRes, queuesRes] = await Promise.all([
            fetch(`${API_URL}/groups/${groupId}`, {
                headers: authHeaders()
            }),
            fetch(`${API_URL}/groups/${groupId}/queues`, {
                headers: authHeaders()
            })
        ]);
        
        const group = await parseJsonResponse(groupRes, 'Failed to load group');
        const queues = await parseJsonResponse(queuesRes, 'Failed to load queues');
        
        content.innerHTML = `
            <div class="group-header">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <h2 class="group-title">${escapeHtml(group.name)}</h2>
                        <p class="group-subtitle">${escapeHtml(group.description || '')}</p>
                    </div>
                    <button class="create-btn">+ Create Event</button>
                </div>
            </div>
            <div class="events-table">
                <div class="events-header">
                    <h3>Events</h3>
                    <p>Manage queue events and view participant lists</p>
                </div>
                ${queues.length === 0 ? `
                    <div class="placeholder">No events in this group yet</div>
                ` : `
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Event Name</th>
                                <th>Date</th>
                                <th>Registration</th>
                                <th>Limit</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${queues.map(queue => `
                                <tr>
                                    <td>${escapeHtml(queue.title)}</td>
                                    <td>${formatDate(queue.eventDate)}</td>
                                    <td>${formatDate(queue.regOpen)} - ${formatDate(queue.regClose)}</td>
                                    <td>${queue.maxSize}</td>
                                    <td>
                                        <span class="status-badge ${queue.isActive ? 'open' : 'closed'}">
                                            ${queue.isActive ? 'Open' : 'Closed'}
                                        </span>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `}
            </div>
        `;
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load group</div>';
    }
}

async function renderBrowseGroups() {
    pageTitle.textContent = 'Browse Groups';

    try {
        const res = await fetch(`${API_URL}/groups`, {
            headers: authHeaders()
        });
        const groups = await parseJsonResponse(res, 'Failed to load groups');

        content.innerHTML = `
            ${groups.length === 0 ? `
                <div class="placeholder">
                    <h3>No groups found</h3>
                    <p>There are no groups to join yet.</p>
                </div>
            ` : `
                <div class="groups-grid">
                    ${groups.map(group => `
                        <div class="group-card">
                            <span class="group-status open">Open</span>
                            <h3 class="group-name">${escapeHtml(group.name)}</h3>
                            <p class="group-description">${escapeHtml(group.description || 'No description')}</p>
                            <div class="group-card-actions">
                                <button class="create-btn join-group-btn" data-group-id="${group.groupId}">Join</button>
                                <button class="secondary-btn view-group-btn" data-group-id="${group.groupId}">View</button>
                            </div>
                            <div class="form-message" data-message-for="${group.groupId}"></div>
                        </div>
                    `).join('')}
                </div>
            `}
        `;
        bindBrowseGroupControls();
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load groups</div>';
    }
}

function bindBrowseGroupControls() {
    document.querySelectorAll('.join-group-btn').forEach(button => {
        button.addEventListener('click', async () => {
            const groupId = button.dataset.groupId;
            const message = document.querySelector(`[data-message-for="${groupId}"]`);
            button.disabled = true;
            if (message) message.textContent = '';

            try {
                const res = await fetch(`${API_URL}/groups/${groupId}/members/me`, {
                    method: 'POST',
                    headers: authHeaders()
                });
                await parseJsonResponse(res, 'Failed to join group');
                if (message) message.textContent = 'Joined successfully';
            } catch (err) {
                if (message) message.textContent = err.message || 'Failed to join group';
            } finally {
                button.disabled = false;
            }
        });
    });

    document.querySelectorAll('.view-group-btn').forEach(button => {
        button.addEventListener('click', () => {
            navigateTo('group-detail', { groupId: button.dataset.groupId });
        });
    });
}

function renderNotifications() {
    pageTitle.textContent = 'Notifications';
    content.innerHTML = `
        <div class="placeholder">
            <div class="placeholder-icon">🔔</div>
            <h3>Notifications</h3>
            <p>No new notifications</p>
        </div>
    `;
}

async function renderProfile() {
    pageTitle.textContent = 'Profile';

    try {
        const user = currentUser || await refreshProfile();
        content.innerHTML = `
            <form class="panel-form profile-form" id="profileForm">
                <label>
                    <span>Email</span>
                    <input name="email" type="email" value="${escapeHtml(user.email)}" readonly>
                </label>
                <label>
                    <span>First Name</span>
                    <input name="firstName" type="text" value="${escapeHtml(user.firstName)}" required>
                </label>
                <label>
                    <span>Last Name</span>
                    <input name="lastName" type="text" value="${escapeHtml(user.lastName)}" required>
                </label>
                <div class="profile-meta">
                    <span>Role: ${escapeHtml(user.role)}</span>
                    <span>Created: ${formatDate(user.createdAt)}</span>
                </div>
                <div class="form-row">
                    <button type="submit" class="create-btn">Save Profile</button>
                </div>
                <div class="form-message" id="profileMessage"></div>
            </form>
        `;
        bindProfileForm();
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load profile</div>';
    }
}

function bindProfileForm() {
    const form = document.getElementById('profileForm');
    const message = document.getElementById('profileMessage');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        message.textContent = '';

        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;

        try {
            const payload = {
                firstName: form.elements.firstName.value.trim(),
                lastName: form.elements.lastName.value.trim()
            };
            const res = await fetch(`${API_URL}/users/me`, {
                method: 'PATCH',
                headers: authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(payload)
            });
            currentUser = await parseJsonResponse(res, 'Failed to save profile');
            await loadUserProfile();
            message.textContent = 'Profile saved';
        } catch (err) {
            message.textContent = err.message || 'Failed to save profile';
        } finally {
            submitBtn.disabled = false;
        }
    });
}

window.navigateTo = navigateTo;

//Инициализация
logoutBtn.addEventListener('click', () => {
    sessionStorage.removeItem('authToken');
    window.location.href = '/index.html';
});

//Обработчики навигации
navItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const view = item.dataset.view;
        navigateTo(view);
    });
});

// Загрузка при старте
loadUserProfile();
navigateTo('my-groups');
