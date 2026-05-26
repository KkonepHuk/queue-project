const API_URL = 'http://localhost:8080/api/v1';
const USE_MOCK = true;

const token = sessionStorage.getItem('authToken');
if (!token) {
    window.location.href = '/index.html';
}

const content = document.getElementById('content');
const pageTitle = document.getElementById('pageTitle');
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const navItems = document.querySelectorAll('.nav-item');

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
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
            firstName = user.first_name;
            lastName = user.last_name;
            userName.textContent = `${firstName} ${lastName}`;
            const initials = (firstName[0] + (lastName[0] || '')).toUpperCase();
            avatarEl.textContent = initials;
        }
    } catch (err) {
        console.error('Failed to load profile:', err);
        avatarEl.textContent = 'U';
    }
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
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const groups = await res.json();
        
        content.innerHTML = `
            <div class="groups-grid">
                ${groups.map(group => `
                    <div class="group-card" onclick="navigateTo('group-detail', {groupId: '${group.group_id}'})">
                        <span class="group-status ${group.is_active ? 'open' : 'closed'}">
                            ${group.is_active ? 'Open' : 'Closed'}
                        </span>
                        <h3 class="group-name">${group.name}</h3>
                        <p class="group-description">${group.description || 'No description'}</p>
                        <div class="group-stats">
                            <span>👥 ${group.members_count || 0} members</span>
                            <span>📅 ${group.active_events || 0} active events</span>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load groups</div>';
    }
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
                headers: { 'Authorization': `Bearer ${token}` }
            }),
            fetch(`${API_URL}/groups/${groupId}/queues`, {
                headers: { 'Authorization': `Bearer ${token}` }
            })
        ]);
        
        const group = await groupRes.json();
        const queues = await queuesRes.json();
        
        content.innerHTML = `
            <div class="group-header">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <h2 class="group-title">${group.name}</h2>
                        <p class="group-subtitle">${group.description || ''}</p>
                    </div>
                    <button class="create-btn">+ Create Event</button>
                </div>
            </div>
            <div class="events-table">
                <!-- Таблица с очередями -->
            </div>
        `;
    } catch (err) {
        content.innerHTML = '<div class="placeholder">Failed to load group</div>';
    }
}

//Заглушки для остальных разделов
function renderBrowseGroups() {
    pageTitle.textContent = 'Browse Groups';
    content.innerHTML = `
        <div class="placeholder">
            <div class="placeholder-icon">🔍</div>
            <h3>Browse Groups</h3>
            <p>Coming soon...</p>
        </div>
    `;
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

function renderProfile() {
    pageTitle.textContent = 'Profile';
    content.innerHTML = `
        <div class="placeholder">
            <div class="placeholder-icon">👤</div>
            <h3>Profile Settings</h3>
            <p>Edit your profile information</p>
        </div>
    `;
}

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
