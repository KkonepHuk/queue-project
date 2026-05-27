const API_BASE = '/api/v1';
const userCache = new Map();

async function request(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const token = sessionStorage.getItem('authToken');
    if (token && options.auth !== false) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    // Only force-logout on 401 for authenticated requests (not for login/register).
    if (response.status === 401 && options.auth !== false) {
        sessionStorage.removeItem('authToken');
        window.location.href = '/index.html';
        throw new Error('Session expired');
    }

    if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || errData.detail || `Server error: ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

function toUserDto(user) {
    return {
        user_id: user.userId,
        email: user.email,
        first_name: user.firstName,
        last_name: user.lastName,
        role: user.role,
        created_at: user.createdAt,
        is_active: user.active
    };
}

function toGroupDto(group) {
    return {
        group_id: group.groupId,
        name: group.name,
        description: group.description,
        created_by: group.createdBy,
        created_at: group.createdAt,
        is_active: true
    };
}

function toQueueDto(queue, participants = []) {
    return {
        queue_id: queue.queueId,
        group_id: queue.groupId,
        created_by: queue.createdBy,
        title: queue.title,
        description: queue.description,
        event_date: queue.eventDate,
        reg_open: queue.regOpen,
        reg_close: queue.regClose,
        max_size: queue.maxSize,
        is_active: queue.isActive,
        created_at: queue.createdAt,
        participants,
        swap_requests: []
    };
}

function toParticipantDto(entry) {
    const rawStatus = String(entry.status || 'WAITING').toLowerCase();
    // Legacy: "skipped" used to be a status, but now skip is an action that keeps WAITING.
    const status = rawStatus === 'skipped' ? 'waiting' : rawStatus;
    return {
        user_id: entry.userId,
        position: entry.position,
        status,
        joined_at: entry.joinedAt
    };
}

function toNotificationDto(notification) {
    return {
        id: notification.notificationId,
        type: String(notification.type || 'SYSTEM').toLowerCase(),
        title: notification.groupName || notification.queueTitle || notification.type || 'Notification',
        message: notification.message,
        timestamp: notification.createdAt || notification.scheduledAt,
        read: notification.status === 'READ'
    };
}

function toGroupMemberDto(m) {
    return {
        group_member_id: m.groupMemberId,
        group_id: m.groupId,
        user_id: m.userId,
        role: String(m.role || 'MEMBER').toLowerCase(),
        joined_at: m.joinedAt
    };
}

function toBackendProfile(data) {
    return {
        firstName: data.first_name,
        lastName: data.last_name
    };
}

function toBackendRegistration(data) {
    return {
        email: data.email,
        password: data.password,
        firstName: data.first_name ?? data.firstName,
        lastName: data.last_name ?? data.lastName
    };
}

function toBackendQueue(data) {
    return {
        title: data.title,
        description: data.description,
        eventDate: data.event_date,
        regOpen: data.reg_open,
        regClose: data.reg_close,
        maxSize: data.max_size
    };
}

export const Api = {
    async login(credentials) {
        return request('/users/login', {
            method: 'POST',
            auth: false,
            body: JSON.stringify(credentials)
        });
    },

    async register(data) {
        const user = await request('/users/register', {
            method: 'POST',
            auth: false,
            body: JSON.stringify(toBackendRegistration(data))
        });
        return toUserDto(user);
    },

    async getProfile() {
        return toUserDto(await request('/users/me'));
    },

    async getUser(userId) {
        const key = String(userId);
        if (userCache.has(key)) return userCache.get(key);
        const u = toUserDto(await request(`/users/${userId}`));
        userCache.set(key, u);
        return u;
    },

    async updateProfile(data) {
        return toUserDto(await request('/users/me', {
            method: 'PATCH',
            body: JSON.stringify(toBackendProfile(data))
        }));
    },

    async getGroups() {
        const groups = await request('/groups');
        return groups.map(toGroupDto);
    },

    async getBrowseGroups() {
        const groups = await request('/groups/browse');
        return groups.map(toGroupDto);
    },

    async createGroup(data) {
        return toGroupDto(await request('/groups', {
            method: 'POST',
            body: JSON.stringify({
                name: data.name,
                description: data.description
            })
        }));
    },

    async joinGroup(groupId) {
        await request(`/groups/${groupId}/members/me`, { method: 'POST' });
        return { success: true };
    },

    async leaveGroup(groupId) {
        await request(`/groups/${groupId}/members/me`, { method: 'DELETE' });
        return { success: true };
    },

    async getGroupMembers(groupId) {
        const members = await request(`/groups/${groupId}/members`);
        return members.map(toGroupMemberDto);
    },

    async removeGroupMember(groupId, memberId) {
        await request(`/groups/${groupId}/members/${memberId}`, { method: 'DELETE' });
        return { success: true };
    },

    async updateGroupMemberRole(groupId, memberId, role) {
        await request(`/groups/${groupId}/members/${memberId}`, {
            method: 'PATCH',
            body: JSON.stringify({ role })
        });
        return { success: true };
    },

    async getQueues(groupId) {
        const queues = await request(`/groups/${groupId}/queues`);
        const enriched = await Promise.all(queues.map(async (queue) => {
            try {
                const entries = await request(`/queues/${queue.queueId}/entries`);
                return toQueueDto(queue, entries.map(toParticipantDto));
            } catch (e) {
                // Don't lie with "0 participants" if we couldn't load entries.
                return toQueueDto(queue, null);
            }
        }));
        return enriched;
    },

    async createQueue(groupId, data) {
        const queue = await request(`/groups/${groupId}/queues`, {
            method: 'POST',
            body: JSON.stringify(toBackendQueue(data))
        });
        return toQueueDto(queue);
    },

    async deleteQueue(queueId) {
        await request(`/queues/${queueId}`, { method: 'DELETE' });
        return { success: true };
    },

    async joinQueue(groupId, queueId) {
        await request(`/queues/${queueId}/join`, { method: 'POST' });
        return { success: true };
    },

    async leaveQueue(groupId, queueId) {
        await request(`/queues/${queueId}/leave`, { method: 'DELETE' });
        return { success: true };
    },

    async getQueueEntries(queueId) {
        const entries = await request(`/queues/${queueId}/entries`);
        return entries.map(toParticipantDto);
    },

    async updateQueueEntryStatus(queueId, userId, status) {
        await request(`/queues/${queueId}/entries/${userId}`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
        return { success: true };
    },

    async getSwapRequests() {
        return [];
    },

    async requestSwap() {
        throw new Error('Swap requests are not implemented on the backend yet');
    },

    async respondSwap() {
        throw new Error('Swap requests are not implemented on the backend yet');
    },

    async getNotifications() {
        const notifications = await request('/notifications').catch(() => []);
        return notifications.map(toNotificationDto);
    },

    async markAsRead(id) {
        await request(`/notifications/${id}/read`, { method: 'PATCH' });
        return { success: true };
    },

    async clearNotifications() {
        await request('/notifications/read-all', { method: 'PATCH' });
        return { success: true };
    },

    async deleteNotifications() {
        await request('/notifications', { method: 'DELETE' });
        return { success: true };
    }
};
