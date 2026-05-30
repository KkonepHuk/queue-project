import { Api } from './api.js';

const token = sessionStorage.getItem('authToken');
if (!token) window.location.href = '/login';

const content = document.getElementById('content');
const pageTitle = document.getElementById('pageTitle');
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const navItems = document.querySelectorAll('.nav-item');
let liveIntervalId = null;
let liveGroupId = null;

async function loadUserProfile() {
    try {
        const user = await Api.getProfile();
        userName.textContent = `${user.first_name} ${user.last_name}`;
        const initials = (user.first_name[0] + (user.last_name[0] || '')).toUpperCase();
        document.querySelector('.avatar').textContent = initials;
        updateNotificationsBadge();
    } catch (err) {
        window.location.href = '/login';
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
    // Stop live polling when leaving group detail.
    if (view !== 'group-detail') stopLivePolling();
    navItems.forEach(item => item.classList.toggle('active', item.dataset.view === view));
    switch(view) {
        case 'my-groups': renderMyGroups(); break;
        case 'group-detail': renderGroupDetail(params.groupId); break;
        case 'browse-groups': renderBrowseGroups(); break;
        case 'notifications': renderNotifications(); break;
        case 'profile': renderProfile(); break;
    }
}

function stopLivePolling() {
    if (liveIntervalId) clearInterval(liveIntervalId);
    liveIntervalId = null;
    liveGroupId = null;
}

function startLivePolling(groupId) {
    stopLivePolling();
    liveGroupId = groupId;
    // First tick quickly, then every 5s.
    setTimeout(() => updateQueueLiveState(groupId), 250);
    liveIntervalId = setInterval(() => updateQueueLiveState(groupId), 5000);
}

async function updateQueueLiveState(groupId) {
    // Only update if we are still on the same group detail page.
    if (!liveGroupId || liveGroupId !== groupId) return;
    const table = document.getElementById('eventsTableBody');
    if (!table) return;

    try {
        const queues = await Api.getQueues(groupId);
        await Promise.all(queues.map(async (q) => {
            const row = table.querySelector(`tr[data-queue-id="${q.queue_id}"]`);
            if (!row) return;

            const participantsArr = Array.isArray(q.participants) ? q.participants : null;
            const count = participantsArr ? participantsArr.length : null;
            const countCell = row.querySelector('.count-cell');
            if (countCell) {
                countCell.textContent = `${typeof count === 'number' ? count : '?'} / ${q.max_size}`;
            }

            // Update queue phase badge live as well.
            const phaseSpan = row.querySelector('.phase-badge');
            if (phaseSpan) {
                const { badgeClass, label } = getQueuePhase(q, Date.now());
                phaseSpan.className = `status-badge ${badgeClass} phase-badge`;
                phaseSpan.textContent = label;
            }

            const currentPosCell = row.querySelector('.current-pos-cell');
            const currentPartCell = row.querySelector('.current-part-cell');
            if (!currentPosCell || !currentPartCell) return;

            if (!participantsArr) {
                currentPosCell.textContent = '?';
                currentPartCell.textContent = '?';
                return;
            }

            const waiting = participantsArr
                .filter(p => p.status === 'waiting')
                .sort((a, b) => (a.position ?? 999999) - (b.position ?? 999999));
            const cur = waiting[0];
            if (!cur) {
                currentPosCell.textContent = '—';
                currentPartCell.textContent = '—';
                return;
            }

            // Resolve name best-effort (cached in Api.getUser()).
            let name = `User ${String(cur.user_id).slice(0, 8)}`;
            try {
                const u = await Api.getUser(cur.user_id);
                name = (`${u.first_name || ''} ${u.last_name || ''}`.trim() || u.email || name);
            } catch {}

            currentPosCell.textContent = `${cur.position}`;
            currentPartCell.textContent = name;
        }));
    } catch (e) {
        // Silent: live refresh shouldn't break the page.
    }
}

function getQueuePhase(queue, nowMs) {
    const regOpenMs = queue.reg_open ? new Date(queue.reg_open).getTime() : NaN;
    const regCloseMs = queue.reg_close ? new Date(queue.reg_close).getTime() : NaN;
    const eventMs = queue.event_date ? new Date(queue.event_date).getTime() : NaN;
    const activeFlag = !!queue.is_active;

    if (!activeFlag) return { badgeClass: 'closed', label: 'Closed' };

    if (Number.isFinite(regOpenMs) && nowMs < regOpenMs) {
        return { badgeClass: 'pending', label: 'Awaiting Registration' };
    }
    if (Number.isFinite(regOpenMs) && Number.isFinite(regCloseMs) && nowMs >= regOpenMs && nowMs <= regCloseMs) {
        return { badgeClass: 'registration', label: 'Registration' };
    }
    if (Number.isFinite(regCloseMs) && Number.isFinite(eventMs) && nowMs > regCloseMs && nowMs < eventMs) {
        return { badgeClass: 'awaiting', label: 'Awaiting Event' };
    }
    if (Number.isFinite(eventMs) && nowMs >= eventMs) {
        return { badgeClass: 'active', label: 'Active Queue' };
    }

    // Fallback: if dates are missing/odd, treat as closed-ish.
    return { badgeClass: 'closed', label: 'Closed' };
}

function formatParticipantStatus(status) {
    const s = String(status || 'waiting').toLowerCase();
    switch (s) {
        case 'waiting': return { cls: 'waiting', label: 'Waiting' };
        case 'passed': return { cls: 'passed', label: 'Answered' };
        case 'skipped': return { cls: 'skipped', label: 'Skipped' };
        default: return { cls: s, label: s };
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
        const members = await Api.getGroupMembers(groupId).catch(() => []);
        const myMember = members.find(m => m.user_id === myId);
        const isManager = myMember?.role === 'owner' || myMember?.role === 'moderator';

        content.innerHTML = `
            <div class="group-header">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <h2 class="group-title">${group.name}</h2>
                        <p class="group-subtitle">${group.description || 'No description'}</p>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button class="btn btn-danger btn-sm leave-group-btn">Leave Group</button>
                        <button class="btn btn-secondary btn-sm members-btn">Members</button>
                        <button class="create-btn" id="openEventModal">+ Create Event</button>
                    </div>
                </div>
                <div class="group-feedback" id="groupFeedback" aria-live="polite"></div>
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
                                <th>Current Position</th>
                                <th>Current Participant</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="eventsTableBody">
                            ${queues.map(queue => {
                                const participantsArr = Array.isArray(queue.participants) ? queue.participants : null;
                                const isJoined = participantsArr ? participantsArr.some(p => p.user_id === myId) : false;
                                const count = participantsArr ? participantsArr.length : null;
                                const nowMs = Date.now();
                                const regOpenMs = queue.reg_open ? new Date(queue.reg_open).getTime() : null;
                                const regCloseMs = queue.reg_close ? new Date(queue.reg_close).getTime() : null;
                                const regOk = (!regOpenMs || nowMs >= regOpenMs) && (!regCloseMs || nowMs <= regCloseMs);
                                const notFull = (typeof queue.max_size === 'number' && typeof count === 'number') ? count < queue.max_size : true;
                                const canJoin = !!queue.is_active && regOk && notFull;
                                const phase = getQueuePhase(queue, nowMs);
                                const canDeleteQueue = isManager || queue.created_by === myId;
                                let joinDisabledTitle = '';
                                if (!queue.is_active) {
                                    joinDisabledTitle = 'Очередь закрыта';
                                } else if (!regOk) {
                                    if (regOpenMs && nowMs < regOpenMs) {
                                        joinDisabledTitle = `Регистрация откроется: ${new Date(regOpenMs).toLocaleString('ru-RU')}`;
                                    } else if (regCloseMs && nowMs > regCloseMs) {
                                        joinDisabledTitle = `Регистрация закрылась: ${new Date(regCloseMs).toLocaleString('ru-RU')}`;
                                    } else {
                                        joinDisabledTitle = 'Регистрация сейчас закрыта';
                                    }
                                } else if (!notFull) {
                                    joinDisabledTitle = 'Очередь заполнена';
                                }
                                const waiting = participantsArr
                                    ? participantsArr.filter(p => p.status === 'waiting').sort((a, b) => (a.position ?? 999999) - (b.position ?? 999999))
                                    : null;
                                const cur = waiting && waiting.length ? waiting[0] : null;
                                return `
                                    <tr data-queue-id="${queue.queue_id}">
                                        <td>${queue.title}</td>
                                        <td>${new Date(queue.event_date).toLocaleDateString('ru-RU')}</td>
                                        <td><span class="status-badge ${phase.badgeClass} phase-badge">${phase.label}</span></td>
                                        <td class="count-cell">${(typeof count === 'number' ? count : '?')} / ${queue.max_size}</td>
                                        <td class="current-pos-cell">${cur ? `${cur.position}` : (participantsArr ? '—' : '?')}</td>
                                        <td class="current-part-cell">${cur ? 'Loading...' : (participantsArr ? '—' : '?')}</td>
                                        <td>
                                            <div class="row-actions">
                                                <button class="btn btn-secondary btn-sm details-btn" data-queue-id="${queue.queue_id}" title="Event details">ℹ️</button>
                                                <button class="btn btn-secondary btn-sm list-btn" data-queue-id="${queue.queue_id}">👥 List</button>
                                                ${canDeleteQueue ? `<button class="btn btn-danger btn-sm delete-queue-btn" data-queue-id="${queue.queue_id}" title="Delete queue">🗑️</button>` : ''}
                                                ${isJoined 
                                                    ? `<button class="btn btn-danger btn-sm leave-btn" data-queue-id="${queue.queue_id}">Leave Queue</button>` 
                                                    : `<button class="btn btn-primary btn-sm join-btn" data-queue-id="${queue.queue_id}" ${!canJoin ? 'disabled' : ''} ${!canJoin && joinDisabledTitle ? `title="${joinDisabledTitle}"` : ''}>Join Queue</button>`
                                                }
                                            </div>
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
                    const feedback = document.getElementById('groupFeedback');
                    if (feedback) {
                        feedback.textContent = err.message || 'Failed to join queue';
                        feedback.className = 'group-feedback error';
                    }
                }
            });
        });

        // Привязка кнопки Leave
        document.querySelectorAll('.leave-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const qId = e.target.dataset.queueId;
                const ok = await openConfirmModal({
                    title: 'Leave Queue',
                    message: 'Are you sure you want to leave this queue?',
                    confirmText: 'Leave',
                    cancelText: 'Cancel',
                    confirmClass: 'btn-danger'
                });
                if (ok) {
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

        // Привязка кнопки Details (описание и даты)
        document.querySelectorAll('.details-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const qId = btn.dataset.queueId;
                const q = queues.find(x => x.queue_id === qId);
                if (q) openEventDetailsModal(q);
            });
        });

        // Привязка кнопки Delete Queue (creator/owner only)
        document.querySelectorAll('.delete-queue-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const qId = btn.dataset.queueId;
                const q = queues.find(x => x.queue_id === qId);
                const ok = await openConfirmModal({
                    title: 'Delete Queue',
                    message: `Delete "${q?.title || 'this queue'}"? This will remove all its entries.`,
                    confirmText: 'Delete',
                    cancelText: 'Cancel',
                    confirmClass: 'btn-danger'
                });
                if (!ok) return;
                try {
                    await Api.deleteQueue(qId);
                    await renderGroupDetail(groupId);
                } catch (err) {
                    const feedback = document.getElementById('groupFeedback');
                    if (feedback) {
                        feedback.textContent = err.message || 'Failed to delete queue';
                        feedback.className = 'group-feedback error';
                    }
                }
            });
        });

        // Привязка кнопки Create Event
        setTimeout(() => {
            document.getElementById('openEventModal')?.addEventListener('click', () => openEventModal(groupId));
        }, 0);

        // Привязка кнопки Members (участники группы)
        const membersBtn = document.querySelector('.members-btn');
        if (membersBtn) {
            membersBtn.addEventListener('click', () => openGroupMembersModal(groupId, group.name));
        }

        // Привязка кнопки Leave Group
        const leaveGroupBtn = document.querySelector('.leave-group-btn');
        if (leaveGroupBtn) {
            leaveGroupBtn.addEventListener('click', async () => {
                const ok = await openConfirmModal({
                    title: 'Leave Group',
                    message: `Are you sure you want to leave "${group.name}"?`,
                    confirmText: 'Leave',
                    cancelText: 'Cancel',
                    confirmClass: 'btn-danger'
                });
                if (ok) {
                    try {
                        const feedback = document.getElementById('groupFeedback');
                        leaveGroupBtn.disabled = true;
                        if (feedback) {
                            feedback.textContent = '';
                            feedback.className = 'group-feedback';
                        }
                        await Api.leaveGroup(groupId);
                        // Success: navigate away quietly (show feedback only on errors).
                        navigateTo('my-groups');
                    } catch (err) {
                        const feedback = document.getElementById('groupFeedback');
                        if (feedback) {
                            feedback.textContent = err.message || 'Failed to leave group';
                            feedback.classList.add('error');
                        }
                        leaveGroupBtn.disabled = false;
                    }
                }
            });
        };

        // Start live polling for queue state (who is "now serving").
        startLivePolling(groupId);
        await updateQueueLiveState(groupId);
    } catch (err) {
        console.error(err);
        content.innerHTML = '<div class="error">Failed to load group details</div>';
    }
}

function openEventDetailsModal(queue) {
    document.getElementById('eventDetailsModal')?.remove();
    const eventDt = queue.event_date ? new Date(queue.event_date).toLocaleString('ru-RU') : '';
    const regOpen = queue.reg_open ? new Date(queue.reg_open).toLocaleString('ru-RU') : '';
    const regClose = queue.reg_close ? new Date(queue.reg_close).toLocaleString('ru-RU') : '';
    const desc = (queue.description || '').trim();

    const html = `
        <div class="modal-overlay active" id="eventDetailsModal" role="dialog" aria-modal="true">
            <div class="modal-box" style="max-width: 560px;">
                <div class="modal-header">
                    <h3>${queue.title}</h3>
                    <button class="close-modal" id="closeEventDetails" aria-label="Close">&times;</button>
                </div>
                <div style="display:grid; gap: 10px; color:#2c3e50;">
                    ${desc ? `<div style="white-space: pre-wrap; color:#34495e;">${desc}</div>` : `<div style="color:#7f8c8d;">No description</div>`}
                    <div style="font-size: 14px;">
                        <div><b>Event date:</b> ${eventDt}</div>
                        <div><b>Registration opens:</b> ${regOpen}</div>
                        <div><b>Registration closes:</b> ${regClose}</div>
                        <div><b>Max size:</b> ${queue.max_size}</div>
                    </div>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);

    const close = () => closeModal('eventDetailsModal');
    document.getElementById('closeEventDetails')?.addEventListener('click', close);
    document.getElementById('eventDetailsModal')?.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) close();
    });
    const onKeyDown = (e) => {
        if (e.key === 'Escape') close();
    };
    document.addEventListener('keydown', onKeyDown, { once: true });
}

async function openGroupMembersModal(groupId, groupName) {
    const myProfile = await Api.getProfile();
    const myId = myProfile.user_id;

    const members = await Api.getGroupMembers(groupId);
    const me = members.find(m => m.user_id === myId);
    const isOwner = me?.role === 'owner';
    const isManager = me?.role === 'owner' || me?.role === 'moderator';

    // Fetch user info for display (best-effort).
    const usersById = {};
    await Promise.all(members.map(async (m) => {
        try {
            usersById[m.user_id] = await Api.getUser(m.user_id);
        } catch {
            usersById[m.user_id] = null;
        }
    }));

    const html = `
        <div class="modal-overlay active" id="groupMembersModal" role="dialog" aria-modal="true">
            <div class="modal-box" style="max-width: 760px;">
                <div class="modal-header">
                    <h3>Members: ${groupName}</h3>
                    <button class="close-modal" id="closeGroupMembers">&times;</button>
                </div>
                <div style="max-height: 420px; overflow-y: auto;">
                    <table class="table" style="font-size: 14px;">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Joined</th>
                                ${isManager ? '<th></th>' : ''}
                            </tr>
                        </thead>
                        <tbody>
                            ${members.map((m) => {
                                const u = usersById[m.user_id];
                                const name = u ? `${u.first_name || ''} ${u.last_name || ''}`.trim() : `User ${String(m.user_id).slice(0, 8)}`;
                                const email = u?.email || '';
                                const joined = m.joined_at ? new Date(m.joined_at).toLocaleString() : '';
                                const role = String(m.role || 'member').toUpperCase();
                                const canKick = isManager && m.role !== 'owner' && m.user_id !== myId;
                                const canEditRole = isOwner && m.role !== 'owner' && m.user_id !== myId;
                                return `
                                    <tr>
                                        <td>${name || `User ${String(m.user_id).slice(0, 8)}`}</td>
                                        <td>${email}</td>
                                        <td>
                                            ${canEditRole ? `
                                                <select class="role-select" data-member-id="${m.group_member_id}">
                                                    <option value="MEMBER" ${m.role === 'member' ? 'selected' : ''}>MEMBER</option>
                                                    <option value="MODERATOR" ${m.role === 'moderator' ? 'selected' : ''}>MODERATOR</option>
                                                </select>
                                            ` : role}
                                        </td>
                                        <td>${joined}</td>
                                        ${isManager ? `
                                            <td style="text-align:right;">
                                                ${canKick ? `<button class="btn btn-danger btn-sm kick-member-btn" data-member-id="${m.group_member_id}" data-name="${name}">Remove</button>` : ''}
                                            </td>
                                        ` : ''}
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
                <div class="group-feedback" id="groupMembersFeedback" aria-live="polite"></div>
            </div>
        </div>
    `;

    document.getElementById('groupMembersModal')?.remove();
    document.body.insertAdjacentHTML('beforeend', html);

    const close = () => closeModal('groupMembersModal');
    document.getElementById('closeGroupMembers')?.addEventListener('click', close);
    document.getElementById('groupMembersModal')?.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) close();
    });

    document.querySelectorAll('.kick-member-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const memberId = e.currentTarget.dataset.memberId;
            const name = e.currentTarget.dataset.name || 'this member';
            const ok = await openConfirmModal({
                title: 'Remove Member',
                message: `Remove ${name} from this group? They will also be removed from all queues in this group.`,
                confirmText: 'Remove',
                cancelText: 'Cancel',
                confirmClass: 'btn-danger'
            });
            if (!ok) return;

            const feedback = document.getElementById('groupMembersFeedback');
            try {
                await Api.removeGroupMember(groupId, memberId);
                closeModal('groupMembersModal');
                await renderGroupDetail(groupId);
            } catch (err) {
                if (feedback) {
                    feedback.textContent = err.message || 'Failed to remove member';
                    feedback.className = 'group-feedback error';
                }
            }
        });
    });

    document.querySelectorAll('.role-select').forEach(sel => {
        sel.addEventListener('change', async (e) => {
            const memberId = e.currentTarget.dataset.memberId;
            const role = e.currentTarget.value;
            const feedback = document.getElementById('groupMembersFeedback');
            try {
                await Api.updateGroupMemberRole(groupId, memberId, role);
                if (feedback) {
                    feedback.textContent = 'Role updated';
                    feedback.className = 'group-feedback success';
                }
            } catch (err) {
                if (feedback) {
                    feedback.textContent = err.message || 'Failed to update role';
                    feedback.className = 'group-feedback error';
                }
            }
        });
    });
}

// === МОДАЛЬНОЕ ОКНО: УЧАСТНИКИ И SWAP ===
async function openParticipantsModal(groupId, queueId) {
    try {
        // Ensure only one participants modal exists.
        document.getElementById('participantsModal')?.remove();

        const queues = await Api.getQueues(groupId);
        const queue = queues.find(q => q.queue_id === queueId);
        if (!queue) return;

        const myProfile = await Api.getProfile();
        const myId = myProfile.user_id;
        const members = await Api.getGroupMembers(groupId).catch(() => []);
        const myMember = members.find(m => m.user_id === myId);
        const isManager = myMember?.role === 'owner' || myMember?.role === 'moderator';
        const phase = typeof getQueuePhase === 'function' ? getQueuePhase(queue, Date.now()) : { badgeClass: 'closed' };
        const canAnswerNow = phase.badgeClass === 'active';
        // Swap is not implemented on the backend yet (UI stubs existed in template).
        const requests = [];
        const incomingReq = null;
        const participantsArr = Array.isArray(queue.participants) ? queue.participants : [];

        // Load real user names for participants (best-effort).
        const usersById = {};
        await Promise.all(participantsArr.map(async (p) => {
            if (!p?.user_id || usersById[p.user_id]) return;
            try {
                usersById[p.user_id] = await Api.getUser(p.user_id);
            } catch {
                usersById[p.user_id] = null;
            }
        }));

        const computeCurrentPos = (participants) => {
            const waitingPositions = participants
                .filter(x => x.status === 'waiting')
                .map(x => x.position)
                .filter(pos => typeof pos === 'number');
            return waitingPositions.length ? Math.min(...waitingPositions) : null;
        };

        const buildParticipantRows = (participants) => {
            const currentPos = computeCurrentPos(participants);
            return participants.map(p => {
                const isMe = p.user_id === myId;
                const u = usersById[p.user_id];
                const displayName = u
                    ? (`${u.first_name || ''} ${u.last_name || ''}`.trim() || u.email)
                    : `User ${String(p.user_id).slice(0, 8)}`;
                const canSwap = false;
                const hasPendingReq = false;
                const isCurrent = currentPos !== null && p.status === 'waiting' && p.position === currentPos;
                const canSkip = (isMe || isManager) && p.status === 'waiting';
                const canPass = canAnswerNow && (isMe || isManager) && isCurrent;
                const st = formatParticipantStatus(p.status);
                return `
                    <tr style="${isMe ? 'background: #f0f8ff; font-weight: bold;' : ''}">
                        <td>${p.position}</td>
                        <td>${displayName} ${isMe ? '(You)' : ''}</td>
                        <td>
                            <span class="status-badge ${st.cls}">${st.label}</span>
                        </td>
                        <td>
                            ${canPass ? `<button class="btn btn-sm btn-primary pass-btn" data-user-id="${p.user_id}" data-name="${displayName}">Answered</button>` : ''}
                            ${canSkip ? `<button class="btn btn-sm btn-secondary skip-btn" data-user-id="${p.user_id}" data-name="${displayName}">Skip</button>` : ''}
                            ${canSwap && !hasPendingReq 
                                ? `<button class="btn btn-sm btn-primary swap-btn" data-target="${p.user_id}">Swap</button>` 
                                : hasPendingReq 
                                    ? `<span style="color: orange; font-size: 12px;">Pending...</span>` 
                                    : ''}
                        </td>
                    </tr>
                `;
            }).join('');
        };

        const html = `
            <div class="modal-overlay active" id="participantsModal">
                <div class="modal-box" style="max-width: 600px;">
                    <div class="modal-header">
                        <h3>Participants: ${queue.title}</h3>
                        <button class="close-modal" id="closePartModal">&times;</button>
                    </div>
                    <div class="participants-list" style="max-height: 400px; overflow-y: auto;">
                        ${Array.isArray(queue.participants) && queue.participants.length ? `
                            <table class="table" style="font-size: 14px;">
                                <thead><tr><th>#</th><th>Name</th><th>Status</th><th>Action</th></tr></thead>
                                <tbody id="participantsTbody">
                                    ${buildParticipantRows(queue.participants)}
                                </tbody>
                            </table>
                        ` : (queue.participants === null
                            ? '<p style="text-align:center; padding: 20px;">Failed to load participants.</p>'
                            : '<p style="text-align:center; padding: 20px;">No participants yet.</p>')}
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
        
        // Close handlers
        let pollTimer = null;
        let lastKey = Array.isArray(queue.participants)
            ? queue.participants.map(p => `${p.user_id}:${p.position}:${p.status}`).join('|')
            : null;
        const stopPolling = () => {
            if (pollTimer) clearInterval(pollTimer);
            pollTimer = null;
        };

        const close = () => {
            stopPolling();
            closeModal('participantsModal');
        };
        const closeBtn = document.getElementById('closePartModal');
        const modalOverlay = document.getElementById('participantsModal');
        closeBtn?.addEventListener('click', close);
        modalOverlay?.addEventListener('click', (e) => {
            if (e.target === modalOverlay) close();
        });
        const onKeyDown = (e) => {
            if (e.key === 'Escape') close();
        };
        document.addEventListener('keydown', onKeyDown, { once: true });

        // Swap wiring intentionally disabled until backend is implemented.
        const wireParticipantActions = () => {
            document.querySelectorAll('#participantsModal .skip-btn').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const userId = btn.dataset.userId;
                    const name = btn.dataset.name || 'this participant';
                    const isMe = userId === myId;
                    const ok = await openConfirmModal({
                        title: 'Skip Participant',
                        message: isMe
                            ? 'Skip yourself? You will be moved to the end.'
                            : `Skip ${name}? Participant will be moved to the end.`,
                        confirmText: 'Skip',
                        cancelText: 'Cancel',
                        confirmClass: 'btn-secondary'
                    });
                    if (!ok) return;
                    try {
                        await Api.updateQueueEntryStatus(queueId, userId, 'SKIPPED');
                        await renderGroupDetail(groupId);
                        stopPolling();
                        closeModal('participantsModal');
                        await openParticipantsModal(groupId, queueId);
                    } catch (err) {
                        const feedback = document.getElementById('groupFeedback');
                        if (feedback) {
                            feedback.textContent = err.message || 'Failed to skip participant';
                            feedback.className = 'group-feedback error';
                        } else {
                            alert(err.message);
                        }
                    }
                });
            });

            document.querySelectorAll('#participantsModal .pass-btn').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const userId = btn.dataset.userId;
                    const name = btn.dataset.name || 'this participant';
                    const isMe = userId === myId;
                    const ok = await openConfirmModal({
                        title: 'Mark As Answered',
                        message: isMe
                            ? 'Mark yourself as answered? The queue will move to the next person.'
                            : `Mark ${name} as answered? The queue will move to the next person.`,
                        confirmText: 'Answered',
                        cancelText: 'Cancel',
                        confirmClass: 'btn-primary'
                    });
                    if (!ok) return;
                    try {
                        await Api.updateQueueEntryStatus(queueId, userId, 'PASSED');
                        await renderGroupDetail(groupId);
                        stopPolling();
                        closeModal('participantsModal');
                        await openParticipantsModal(groupId, queueId);
                    } catch (err) {
                        const feedback = document.getElementById('groupFeedback');
                        if (feedback) {
                            feedback.textContent = err.message || 'Failed to mark as answered';
                            feedback.className = 'group-feedback error';
                        } else {
                            alert(err.message);
                        }
                    }
                });
            });
        };

        wireParticipantActions();

        // Poll entries so "who is current" updates for everyone without reload.
        const refreshParticipants = async () => {
            const latest = await Api.getQueueEntries(queueId).catch(() => null);
            if (!latest) return;

            const nextKey = latest.map(p => `${p.user_id}:${p.position}:${p.status}`).join('|');
            if (nextKey === lastKey) return;
            lastKey = nextKey;

            // Best-effort: resolve names for any new users.
            await Promise.all(latest.map(async (p) => {
                if (!p?.user_id || usersById[p.user_id]) return;
                try {
                    usersById[p.user_id] = await Api.getUser(p.user_id);
                } catch {
                    usersById[p.user_id] = null;
                }
            }));

            const tbody = document.getElementById('participantsTbody');
            if (!tbody) return;
            const listEl = document.querySelector('#participantsModal .participants-list');
            const prevScroll = listEl ? listEl.scrollTop : 0;
            tbody.innerHTML = buildParticipantRows(latest);
            if (listEl) listEl.scrollTop = prevScroll;
            wireParticipantActions();
        };
        pollTimer = setInterval(refreshParticipants, 3000);

        // Swap wiring intentionally disabled until backend is implemented.
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

function openConfirmModal({
    title = 'Confirm',
    message = 'Are you sure?',
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    confirmClass = 'btn-primary'
} = {}) {
    return new Promise((resolve) => {
        document.getElementById('confirmModal')?.remove();

        const html = `
            <div class="modal-overlay active" id="confirmModal" role="dialog" aria-modal="true">
                <div class="modal-box">
                    <div class="modal-header">
                        <h3>${title}</h3>
                        <button class="close-modal" id="confirmCloseBtn" aria-label="Close">&times;</button>
                    </div>
                    <div style="color:#2c3e50; line-height: 1.4;">${message}</div>
                    <div class="modal-actions">
                        <button class="btn btn-secondary" id="confirmCancelBtn">${cancelText}</button>
                        <button class="btn ${confirmClass}" id="confirmOkBtn">${confirmText}</button>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);

        const overlay = document.getElementById('confirmModal');
        const okBtn = document.getElementById('confirmOkBtn');
        const cancelBtn = document.getElementById('confirmCancelBtn');
        const closeBtn = document.getElementById('confirmCloseBtn');

        const cleanup = () => {
            document.removeEventListener('keydown', onKeyDown);
            overlay?.classList.remove('active');
            setTimeout(() => overlay?.remove(), 200);
        };

        const finish = (val) => {
            cleanup();
            resolve(val);
        };

        const onKeyDown = (e) => {
            if (e.key === 'Escape') finish(false);
        };

        document.addEventListener('keydown', onKeyDown);

        overlay?.addEventListener('click', (e) => {
            if (e.target === overlay) finish(false);
        });
        okBtn?.addEventListener('click', () => finish(true));
        cancelBtn?.addEventListener('click', () => finish(false));
        closeBtn?.addEventListener('click', () => finish(false));

        okBtn?.focus();
    });
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
                    <div class="group-feedback" id="eventFeedback" aria-live="polite" style="margin-bottom: 8px;"></div>
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
    const feedback = document.getElementById('eventFeedback');
    
    const payload = {
        title: document.getElementById('eventTitle').value,
        description: document.getElementById('eventDesc').value,
        event_date: document.getElementById('eventDate').value,
        reg_open: document.getElementById('regOpen').value,
        reg_close: document.getElementById('regClose').value,
        max_size: parseInt(document.getElementById('eventMaxSize').value)
    };

    if (feedback) {
        feedback.textContent = '';
        feedback.className = 'group-feedback';
    }

    const eventMs = payload.event_date ? new Date(payload.event_date).getTime() : NaN;
    const openMs = payload.reg_open ? new Date(payload.reg_open).getTime() : NaN;
    const closeMs = payload.reg_close ? new Date(payload.reg_close).getTime() : NaN;
    if (!Number.isFinite(eventMs) || !Number.isFinite(openMs) || !Number.isFinite(closeMs)) {
        if (feedback) {
            feedback.textContent = 'Please provide valid dates';
            feedback.className = 'group-feedback error';
        }
        return;
    }
    if (!(openMs < closeMs)) {
        if (feedback) {
            feedback.textContent = 'Registration open must be before registration close';
            feedback.className = 'group-feedback error';
        }
        return;
    }
    if (!(openMs < eventMs)) {
        if (feedback) {
            feedback.textContent = 'Registration open must be before the event date';
            feedback.className = 'group-feedback error';
        }
        return;
    }
    if (closeMs > eventMs) {
        if (feedback) {
            feedback.textContent = 'Registration close must be on/before the event date';
            feedback.className = 'group-feedback error';
        }
        return;
    }

    const now = Date.now();

    if (eventMs < now) {
        feedback.textContent = 'Event date must be in the future';
        feedback.className = 'group-feedback error';
        return;
    }

    if (payload.max_size <= 0) {
        feedback.textContent = 'Max size must be greater than 0';
        feedback.className = 'group-feedback error';
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
        await Api.createQueue(groupId, payload);
        closeModalEvent();
        await renderGroupDetail(groupId);
    } catch (err) {
        if (feedback) {
            feedback.textContent = err.message || 'Failed to create event';
            feedback.className = 'group-feedback error';
        }
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
                <div class="profile-feedback" id="profileFeedback" aria-live="polite"></div>
            </div>
        `;

        // Обработчик кнопки Cancel
        document.getElementById('cancelProfile').onclick = () => navigateTo('my-groups');

        // Обработчик сохранения
        document.getElementById('profileForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button[type="submit"]');
            const feedback = document.getElementById('profileFeedback');
            btn.disabled = true;
            btn.textContent = 'Saving...';
            feedback.textContent = '';
            feedback.className = 'profile-feedback';

            try {
                const updatedUser = await Api.updateProfile({
                    first_name: document.getElementById('firstName').value,
                    last_name: document.getElementById('lastName').value
                });

                // Мгновенно обновляем шапку без перезагрузки
                document.getElementById('userName').textContent = `${updatedUser.first_name} ${updatedUser.last_name}`;
                const initials = (updatedUser.first_name[0] + (updatedUser.last_name[0] || '')).toUpperCase();
                document.querySelector('.avatar').textContent = initials;

                feedback.textContent = 'Saved';
                feedback.classList.add('success');
                navigateTo('my-groups');
                
            } catch (err) {
                feedback.textContent = err.message || 'Error saving profile';
                feedback.classList.add('error');
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
                <div style="display:flex; gap:8px;">
                    <button class="btn btn-sm btn-secondary btn-clear" id="clearNotifs">Mark Read</button>
                    <button class="btn btn-sm btn-danger" id="deleteNotifs">Delete</button>
                </div>
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

        document.getElementById('deleteNotifs').addEventListener('click', async () => {
            const ok = await openConfirmModal({
                title: 'Delete Notifications',
                message: 'Delete all notifications? This cannot be undone.',
                confirmText: 'Delete',
                cancelText: 'Cancel',
                confirmClass: 'btn-danger'
            });
            if (!ok) return;
            await Api.deleteNotifications();
            updateNotificationsBadge();
            renderNotifications();
        });

        // Mark as read on click (no navigation for now).
        document.querySelectorAll('.notif-item').forEach(item => {
            item.addEventListener('click', async () => {
                const id = item.dataset.id;
                if (!id) return;
                if (item.classList.contains('unread')) {
                    try {
                        await Api.markAsRead(id);
                        item.classList.remove('unread');
                        item.classList.add('read');
                        updateNotificationsBadge();
                    } catch {
                        // ignore
                    }
                }
            });
        });
        
    } catch (err) {
        content.innerHTML = '<div class="error">Failed to load notifications</div>';
    }
}

// === ЗАПУСК ===
logoutBtn.addEventListener('click', () => {
    sessionStorage.removeItem('authToken');
    window.location.href = '/login';
});

navItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        navigateTo(item.dataset.view);
    });
});

loadUserProfile();
navigateTo('my-groups');

// Lightweight polling so the badge updates even when you stay on one screen.
setInterval(updateNotificationsBadge, 15000);
