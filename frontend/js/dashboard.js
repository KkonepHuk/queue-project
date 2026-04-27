import { Api } from './api.js';

const token = sessionStorage.getItem('authToken');
if (!token) window.location.href = '/index.html';

const content = document.getElementById('content');
const pageTitle = document.getElementById('pageTitle');
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const navItems = document.querySelectorAll('.nav-item');

async function loadUserProfile() {
    try {
        const user = await Api.getProfile();
        userName.textContent = `${user.first_name} ${user.last_name}`;
        const initials = (user.first_name[0] + (user.last_name[0] || '')).toUpperCase();
        document.querySelector('.avatar').textContent = initials;
        updateNotificationsBadge();
    } catch (err) {
        window.location.href = '/index.html';
    }
}

async function updateNotificationsBadge() {
    try {
        const notifs = await Api.getNotifications();
        const unreadCount = notifs.filter(n => !n.read).length;
        
        const badge = document.querySelector('.nav-item[data-view="notifications"] .badge');
        if (badge) {
            if (unreadCount > 0) {
                badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (err) {
        console.error('Failed to update badge:', err);
    }
}

function navigateTo(view, params = {}) {
    navItems.forEach(item => item.classList.toggle('active', item.dataset.view === view));
    switch(view) {
        case 'my-groups': renderMyGroups(); break;
        case 'group-detail': renderGroupDetail(params.groupId); break;
        case 'browse-groups': renderBrowseGroups(); break;
        case 'notifications': renderNotifications(); break;
        case 'profile': renderProfile(); break;
    }
}

async function renderMyGroups() {
    pageTitle.textContent = 'My Groups';
    try {
        const groups = await Api.getGroups();
        const html = `
            <div class="groups-header">
                <h2>Your Groups</h2>
                <button id="openCreateGroup" class="btn btn-primary">+ Create Group</button>
            </div>
            <div class="groups-grid">
                ${groups.map(group => `
                    <div class="group-card" data-group-id="${group.group_id}">
                        <span class="group-status ${group.is_active ? 'open' : 'closed'}">
                            ${group.is_active ? 'Open' : 'Closed'}
                        </span>
                        <h3 class="group-name">${group.name}</h3>
                        <p class="group-description">${group.description || ''}</p>
                    </div>
                `).join('')}
            </div>
        `;
        content.innerHTML = html;
        
        setTimeout(() => {
            document.getElementById('openCreateGroup')?.addEventListener('click', openModal);
            document.querySelectorAll('.group-card').forEach(card => {
                card.addEventListener('click', () => navigateTo('group-detail', { groupId: card.dataset.groupId }));
            });
        }, 0);
    } catch (err) {
        content.innerHTML = '<div class="error">Failed to load groups</div>';
    }
}

async function renderGroupDetail(groupId) {
    pageTitle.textContent = 'Group Details';
    content.innerHTML = '';
    
    try {
        const groups = await Api.getGroups();
        const group = groups.find(g => g.group_id === groupId);
        if (!group) {
            content.innerHTML = '<div class="error">Group not found</div>';
            return;
        }

        const queues = await Api.getQueues(groupId);
        const myProfile = await Api.getProfile();
        const myId = myProfile.user_id;

        content.innerHTML = `
            <div class="group-header">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <h2 class="group-title">${group.name}</h2>
                        <p class="group-subtitle">${group.description || 'No description'}</p>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button class="btn btn-danger btn-sm leave-group-btn">Leave Group</button>
                        <button class="create-btn" id="openEventModal">+ Create Event</button>
                    </div>
                </div>
            </div>
            <div class="events-table">
                <div class="events-header">
                    <h3>Events</h3>
                    <p>Manage queue events and view participant lists</p>
                </div>
                ${queues.length > 0 ? `
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Event Name</th>
                                <th>Date</th>
                                <th>Status</th>
                                <th>Participants</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${queues.map(queue => {
                                const isJoined = queue.participants?.some(p => p.user_id === myId);
                                const count = queue.participants?.length || 0;
                                return `
                                    <tr>
                                        <td>${queue.title}</td>
                                        <td>${new Date(queue.event_date).toLocaleDateString('ru-RU')}</td>
                                        <td><span class="status-badge ${queue.is_active ? 'open' : 'closed'}">${queue.is_active ? 'Open' : 'Closed'}</span></td>
                                        <td>${count} / ${queue.max_size}</td>
                                        <td style="display: flex; gap: 8px;">
                                            <button class="btn btn-secondary btn-sm list-btn" data-queue-id="${queue.queue_id}">👥 List</button>
                                            ${isJoined 
                                                ? `<button class="btn btn-danger btn-sm leave-btn" data-queue-id="${queue.queue_id}">Leave Queue</button>` 
                                                : `<button class="btn btn-primary btn-sm join-btn" data-queue-id="${queue.queue_id}" ${!queue.is_active ? 'disabled' : ''}>Join Queue</button>`
                                            }
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                ` : `<div style="text-align: center; padding: 40px; color: #7f8c8d;">No events yet. Click "Create Event" to add one.</div>`}
            </div>
        `;

        // Привязка кнопки Join
        document.querySelectorAll('.join-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                try {
                    await Api.joinQueue(groupId, btn.dataset.queueId);
                    await renderGroupDetail(groupId);
                    updateNotificationsBadge();
                } catch (err) {
                    //alert(err.message);
                }
            });
        });

        // Привязка кнопки Leave
        document.querySelectorAll('.leave-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const qId = e.target.dataset.queueId;
                if (confirm('Are you sure you want to leave this queue?')) {
                    try {
                        await Api.leaveQueue(groupId, qId);
                        await renderGroupDetail(groupId);
                    } catch (err) { alert(err.message); }
                }
            });
        });

        // Привязка кнопки List (участники)
        document.querySelectorAll('.list-btn').forEach(btn => {
            btn.addEventListener('click', () => openParticipantsModal(groupId, btn.dataset.queueId));
        });

        // Привязка кнопки Create Event
        setTimeout(() => {
            document.getElementById('openEventModal')?.addEventListener('click', () => openEventModal(groupId));
        }, 0);

        // Привязка кнопки Leave Group
        const leaveGroupBtn = document.querySelector('.leave-group-btn');
        if (leaveGroupBtn) {
            leaveGroupBtn.addEventListener('click', async () => {
                if (confirm(`Are you sure you want to leave "${group.name}"?`)) {
                    try {
                        await Api.leaveGroup(groupId);
                        // После выхода возвращаемся к списку групп
                        navigateTo('my-groups');
                    } catch (err) {
                        alert(err.message);
                    }
                }
            });
        };

    } catch (err) {
        console.error(err);
        content.innerHTML = '<div class="error">Failed to load group details</div>';
    }
}

// === МОДАЛЬНОЕ ОКНО: УЧАСТНИКИ И SWAP ===
async function openParticipantsModal(groupId, queueId) {
    try {
        const queues = await Api.getQueues(groupId);
        const queue = queues.find(q => q.queue_id === queueId);
        if (!queue) return;

        const myProfile = await Api.getProfile();
        const myId = myProfile.user_id;
        const requests = await Api.getSwapRequests(groupId, queueId);
        const incomingReq = requests.find(r => r.target === myId && r.status === 'pending');

        const html = `
            <div class="modal-overlay active" id="participantsModal">
                <div class="modal-box" style="max-width: 600px;">
                    <div class="modal-header">
                        <h3>Participants: ${queue.title}</h3>
                        <button class="close-modal" id="closePartModal">&times;</button>
                    </div>
                    <div class="participants-list" style="max-height: 400px; overflow-y: auto;">
                        ${queue.participants?.length ? `
                            <table class="table" style="font-size: 14px;">
                                <thead><tr><th>#</th><th>Name</th><th>Status</th><th>Action</th></tr></thead>
                                <tbody>
                                    ${queue.participants.map(p => {
                                        const isMe = p.user_id === myId;
                                        const canSwap = queue.participants.some(mp => mp.user_id === myId) && !isMe;
                                        const hasPendingReq = requests.some(r => r.initiator === myId && r.target === p.user_id && r.status === 'pending');
                                        return `
                                            <tr style="${isMe ? 'background: #f0f8ff; font-weight: bold;' : ''}">
                                                <td>${p.position}</td>
                                                <td>${p.name} ${isMe ? '(You)' : ''}</td>
                                                <td><span class="status-badge ${p.status}">${p.status}</span></td>
                                                <td>
                                                    ${canSwap && !hasPendingReq 
                                                        ? `<button class="btn btn-sm btn-primary swap-btn" data-target="${p.user_id}">Swap</button>` 
                                                        : hasPendingReq 
                                                            ? `<span style="color: orange; font-size: 12px;">Pending...</span>` 
                                                            : ''}
                                                </td>
                                            </tr>
                                        `;
                                    }).join('')}
                                </tbody>
                            </table>
                        ` : '<p style="text-align:center; padding: 20px;">No participants yet.</p>'}
                    </div>
                    ${incomingReq ? `
                        <div style="margin-top: 16px; padding: 12px; background: #fff3cd; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                            <span>⚠️ <b>Someone</b> wants to swap with you!</span>
                            <div style="display: flex; gap: 8px;">
                                <button class="btn btn-sm btn-secondary" id="declineSwapBtn">Decline</button>
                                <button class="btn btn-sm btn-primary" id="acceptSwapBtn">Accept</button>
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', html);
        
        // Обработчики — ПРЯМОЕ закрытие без функции closeModal
        const closeBtn = document.getElementById('closeParticipantsModal');
        const modalOverlay = document.getElementById('participantsModal');

        if (closeBtn) {
            closeBtn.addEventListener('click', function() {
                modalOverlay.classList.remove('active');
                setTimeout(() => {
                    if (modalOverlay && modalOverlay.parentNode) {
                        modalOverlay.remove();
                    }
                }, 200);
            });
        }

        if (modalOverlay) {
            modalOverlay.addEventListener('click', function(e) {
                if (e.target === modalOverlay) {
                    modalOverlay.classList.remove('active');
                    setTimeout(() => {
                        if (modalOverlay && modalOverlay.parentNode) {
                            modalOverlay.remove();
                        }
                    }, 200);
                }
            });
        }

        document.querySelectorAll('.swap-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                try {
                    await Api.requestSwap(groupId, queueId, btn.dataset.target);
                    updateNotificationsBadge();
                    openParticipantsModal(groupId, queueId);
                    setTimeout(() => alert('Swap requested!'), 500);
                } catch (err) {
                    alert(err.message);
                }
            });
        });

        if (incomingReq) {
            document.getElementById('acceptSwapBtn').onclick = async () => {
                await Api.respondSwap(groupId, queueId, incomingReq.request_id, true);
                closeModal('participantsModal');
                await renderGroupDetail(groupId);
            };
            document.getElementById('declineSwapBtn').onclick = async () => {
                await Api.respondSwap(groupId, queueId, incomingReq.request_id, false);
                openParticipantsModal(groupId, queueId);
            };
        }
    } catch (err) {
        console.error(err);
    }
}

// === МОДАЛЬНОЕ ОКНО: СОЗДАНИЕ ГРУППЫ ===
function openModal() {
    const html = `
        <div class="modal-overlay active" id="createGroupModal">
            <div class="modal-box">
                <div class="modal-header">
                    <h3>Create New Group</h3>
                    <button class="close-modal" id="closeCreateGroup">&times;</button>
                </div>
                <form id="createGroupForm">
                    <div class="form-group">
                        <label>Group Name *</label>
                        <input type="text" id="groupName" required placeholder="e.g. CS101 Spring 2026">
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea id="groupDesc" placeholder="Short description..."></textarea>
                    </div>
                    <div class="modal-actions">
                        <button type="button" class="btn btn-secondary" id="cancelCreateGroup">Cancel</button>
                        <button type="submit" class="btn btn-primary" id="submitCreateGroup">Create</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);
    
    document.getElementById('closeCreateGroup').onclick = closeModal;
    document.getElementById('cancelCreateGroup').onclick = closeModal;
    document.getElementById('createGroupModal').addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) closeModal();
    });
    document.getElementById('createGroupForm').addEventListener('submit', handleCreateGroup);
}

function closeModal(modalId = 'createGroupModal') {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 200);
    }
}

async function handleCreateGroup(e) {
    e.preventDefault();
    const btn = document.getElementById('submitCreateGroup');
    const name = document.getElementById('groupName').value.trim();
    const description = document.getElementById('groupDesc').value.trim();
    
    if (!name) return;
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
        await Api.createGroup({ name, description });
        closeModal();
        await renderMyGroups();
        updateNotificationsBadge();
    } catch (err) {
        alert('Error: ' + err.message);
        btn.disabled = false;
        btn.textContent = 'Create';
    }
}

// === МОДАЛЬНОЕ ОКНО: СОЗДАНИЕ СОБЫТИЯ (с regOpen/regClose) ===
function openEventModal(groupId) {
    const html = `
        <div class="modal-overlay active" id="createEventModal">
            <div class="modal-box">
                <div class="modal-header">
                    <h3>Create New Event</h3>
                    <button class="close-modal" id="closeEventModal">&times;</button>
                </div>
                <form id="createEventForm">
                    <div class="form-group">
                        <label>Event Title *</label>
                        <input type="text" id="eventTitle" required placeholder="e.g. Final Exam">
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea id="eventDesc" placeholder="Details..."></textarea>
                    </div>
                    <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                        <div class="form-group">
                            <label>Event Date *</label>
                            <input type="datetime-local" id="eventDate" required>
                        </div>
                        <div class="form-group">
                            <label>Max Size *</label>
                            <input type="number" id="eventMaxSize" required placeholder="20">
                        </div>
                    </div>
                    <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                        <div class="form-group">
                            <label>Reg Open *</label>
                            <input type="datetime-local" id="regOpen" required>
                        </div>
                        <div class="form-group">
                            <label>Reg Close *</label>
                            <input type="datetime-local" id="regClose" required>
                        </div>
                    </div>
                    <div class="modal-actions">
                        <button type="button" class="btn btn-secondary" id="cancelEventModal">Cancel</button>
                        <button type="submit" class="btn btn-primary" id="submitEvent">Create</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);
    
    document.getElementById('closeEventModal').onclick = closeModalEvent;
    document.getElementById('cancelEventModal').onclick = closeModalEvent;
    document.getElementById('createEventModal').addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) closeModalEvent();
    });
    document.getElementById('createEventForm').addEventListener('submit', (e) => handleCreateEvent(e, groupId));
}

function closeModalEvent() {
    const modal = document.getElementById('createEventModal');
    if (modal) {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 200);
    }
}

async function handleCreateEvent(e, groupId) {
    e.preventDefault();
    const btn = document.getElementById('submitEvent');
    
    const payload = {
        title: document.getElementById('eventTitle').value,
        description: document.getElementById('eventDesc').value,
        event_date: document.getElementById('eventDate').value,
        reg_open: document.getElementById('regOpen').value,
        reg_close: document.getElementById('regClose').value,
        max_size: parseInt(document.getElementById('eventMaxSize').value)
    };

    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
        await Api.createQueue(groupId, payload);
        closeModalEvent();
        await renderGroupDetail(groupId);
    } catch (err) {
        btn.disabled = false;
        btn.textContent = 'Create';
    }
}

async function renderBrowseGroups() {
    pageTitle.textContent = 'Browse Groups';
    
    try {
        const allGroups = await Api.getBrowseGroups();
        const myGroups = await Api.getGroups();
        const myGroupIds = myGroups.map(g => g.group_id);
        
        // Поле поиска
        const searchHTML = `
            <div class="browse-header">
                <h2>Discover Groups</h2>
                <p>Find and join groups that interest you</p>
                <div class="search-box">
                    <input type="text" id="groupSearch" placeholder="Search groups by name or description..." autocomplete="off">
                </div>
            </div>
            <div class="groups-grid" id="browseGroupsGrid">
                ${allGroups.map(group => {
                    const isJoined = myGroupIds.includes(group.group_id);
                    return `
                        <div class="group-card browse-card" data-name="${group.name.toLowerCase()}" data-desc="${(group.description || '').toLowerCase()}">
                            <span class="group-status ${group.is_active ? 'open' : 'closed'}">
                                ${group.is_active ? 'Open' : 'Closed'}
                            </span>
                            <h3 class="group-name">${group.name}</h3>
                            <p class="group-description">${group.description || 'No description'}</p>
                            <div class="card-actions">
                                ${isJoined 
                                    ? `<button class="btn btn-secondary btn-sm" disabled>✓ Joined</button>`
                                    : `<button class="btn btn-primary btn-sm join-group-btn" data-group-id="${group.group_id}" ${!group.is_active ? 'disabled' : ''}>Join Group</button>`
                                }
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
            <div id="noResults" style="text-align: center; padding: 40px; color: #7f8c8d; display: none;">
                <p style="font-size: 48px; margin-bottom: 16px;">😕</p>
                <p>No groups found matching your search</p>
            </div>
        `;
        
        content.innerHTML = searchHTML;
        
        // Привязка обработчиков к кнопкам Join
        document.querySelectorAll('.join-group-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const groupId = e.target.dataset.groupId;
                try {
                    await Api.joinGroup(groupId);
                    await renderBrowseGroups();
                    updateNotificationsBadge();
                } catch (err) {
                    console.error(err);
                }
            });
        });
        
        // Обработчик поиска
        const searchInput = document.getElementById('groupSearch');
        const groupsGrid = document.getElementById('browseGroupsGrid');
        const noResults = document.getElementById('noResults');
        const cards = groupsGrid.querySelectorAll('.group-card');
        
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            let visibleCount = 0;
            
            cards.forEach(card => {
                const name = card.dataset.name;
                const desc = card.dataset.desc;
                
                if (name.includes(query) || desc.includes(query)) {
                    card.style.display = 'block';
                    visibleCount++;
                } else {
                    card.style.display = 'none';
                }
            });
            
            // Показываем сообщение, если ничего не найдено
            if (visibleCount === 0) {
                noResults.style.display = 'block';
                groupsGrid.style.display = 'none';
            } else {
                noResults.style.display = 'none';
                groupsGrid.style.display = 'grid';
            }
        });
        
    } catch (err) {
        content.innerHTML = '<div class="error">Failed to load groups</div>';
    }
}

async function renderProfile() {
    pageTitle.textContent = 'Profile Settings';
    
    try {
        const user = await Api.getProfile();
        
        // Рисуем форму
        content.innerHTML = `
            <div class="profile-card">
                <h2>Edit Profile</h2>
                <p class="subtitle">Update your personal information</p>
                <form id="profileForm">
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" value="${user.email}" disabled readonly style="background: #ecf0f1; cursor: not-allowed;">
                    </div>
                    <div class="form-group">
                        <label>First Name</label>
                        <input type="text" id="firstName" value="${user.first_name}" required>
                    </div>
                    <div class="form-group">
                        <label>Last Name</label>
                        <input type="text" id="lastName" value="${user.last_name}" required>
                    </div>
                    <div class="modal-actions">
                        <button type="button" class="btn btn-secondary" id="cancelProfile">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save Changes</button>
                    </div>
                </form>
            </div>
        `;

        // Обработчик кнопки Cancel
        document.getElementById('cancelProfile').onclick = () => navigateTo('my-groups');

        // Обработчик сохранения
        document.getElementById('profileForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.textContent = 'Saving...';

            try {
                const updatedUser = await Api.updateProfile({
                    first_name: document.getElementById('firstName').value,
                    last_name: document.getElementById('lastName').value
                });

                // Мгновенно обновляем шапку без перезагрузки
                document.getElementById('userName').textContent = `${updatedUser.first_name} ${updatedUser.last_name}`;
                const initials = (updatedUser.first_name[0] + (updatedUser.last_name[0] || '')).toUpperCase();
                document.querySelector('.avatar').textContent = initials;

                alert('Profile updated successfully!');
                navigateTo('my-groups');
                
            } catch (err) {
                alert('Error saving profile: ' + err.message);
                btn.disabled = false;
                btn.textContent = 'Save Changes';
            }
        });

    } catch (err) {
        content.innerHTML = '<div class="error">Failed to load profile</div>';
    }
}

async function renderNotifications() {
    pageTitle.textContent = 'Notifications';
    try {
        const notifs = await Api.getNotifications();
        updateNotificationsBadge();
        const html = `
            <div class="notif-header">
                <h2>Notifications</h2>
                <button class="btn btn-sm btn-secondary btn-clear" id="clearNotifs">Clear All</button>
            </div>
            <div class="notif-list">
                ${notifs.length === 0 ? `
                    <div class="placeholder">
                        <div class="placeholder-icon">🔔</div>
                        <p>No new notifications</p>
                    </div>
                ` : notifs.map(n => `
                    <div class="notif-item ${n.read ? 'read' : 'unread'}" data-id="${n.id}">
                        <div class="notif-icon">${n.type === 'swap' ? '🔄' : n.type === 'queue' ? '📥' : 'ℹ️'}</div>
                        <div class="notif-content">
                            <div class="notif-title">${n.title}</div>
                            <div class="notif-message">${n.message}</div>
                            <div class="notif-time">${new Date(n.timestamp).toLocaleString('ru-RU', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: 'short' })}</div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
        
        content.innerHTML = html;
        
        document.getElementById('clearNotifs').addEventListener('click', async () => {
            await Api.clearNotifications();
            updateNotificationsBadge();
            renderNotifications();
        });
        
    } catch (err) {
        content.innerHTML = '<div class="error">Failed to load notifications</div>';
    }
}

// === ЗАПУСК ===
logoutBtn.addEventListener('click', () => {
    sessionStorage.removeItem('authToken');
    window.location.href = '/index.html';
});

navItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        navigateTo(item.dataset.view);
    });
});

loadUserProfile();
navigateTo('my-groups');
