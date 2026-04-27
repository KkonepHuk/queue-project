const API_BASE = 'http://localhost:8080/api/v1';
const USE_MOCK = true;

// --- ОБЩАЯ ЛОГИКА ЗАПРОСОВ ---
async function request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const token = sessionStorage.getItem('authToken');
    if (token && options.headers?.['Authorization'] !== null) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, { ...options, headers });
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Server error: ${response.status}`);
        }
        if (response.status === 204) return null;
        return await response.json();
    } catch (error) {
        console.error(`API Request failed [${endpoint}]:`, error);
        throw error;
    }
}

// --- ХРАНИЛИЩЕ ДАННЫХ (С сохранением в LocalStorage) ---

// 1. Группы
const savedGroups = localStorage.getItem('mockGroups');
let mockGroups = savedGroups ? JSON.parse(savedGroups) : [
    { group_id: '1', name: 'Computer Science 101', description: 'Intro to Programming', is_active: true },
    { group_id: '2', name: 'Data Structures', description: 'Advanced algorithms', is_active: false }
];

// 2. Очереди и участники
const savedQueues = localStorage.getItem('mockQueues');
let mockQueues = savedQueues ? JSON.parse(savedQueues) : {
    '1': [
        { 
            queue_id: 'q1', group_id: '1', title: 'Office Hours - Week 12', description: 'Consultation', 
            event_date: '2026-03-25T10:00:00', reg_open: '2026-03-20T00:00:00', reg_close: '2026-03-24T23:59:00', 
            max_size: 20, is_active: true, 
            participants: [
                { user_id: 'user_alice', name: 'Alice Smith', position: 1, status: 'active', joined_at: '2026-03-20T10:00:00' },
                { user_id: 'uuid-1', name: 'John Doe', position: 2, status: 'waiting', joined_at: '2026-03-20T11:00:00' },
                { user_id: 'user_bob', name: 'Bob Johnson', position: 3, status: 'waiting', joined_at: '2026-03-20T12:00:00' }
            ],
            swap_requests: []
        }
    ],
    '2': [] 
};

const MOCKS = {
    login: { token: 'mock-jwt-token-12345' },
    register: { user_id: 'uuid-1', email: 'test@test.com', first_name: 'Mock', last_name: 'User' },
    me: { user_id: 'uuid-1', email: 'test@test.com', first_name: 'John', last_name: 'Doe', role: 'USER' }
};

function saveToStorage() {
    localStorage.setItem('mockGroups', JSON.stringify(mockGroups));
    localStorage.setItem('mockQueues', JSON.stringify(mockQueues));
}

// --- API ФУНКЦИИ ---
export const Api = {
    async login(credentials) {
        if (USE_MOCK) return new Promise(res => setTimeout(() => res(MOCKS.login), 500));
        return request('/users/login', { method: 'POST', body: JSON.stringify(credentials) });
    },
    async register(data) {
        if (USE_MOCK) return new Promise(res => setTimeout(() => res(MOCKS.register), 500));
        return request('/users/register', { method: 'POST', body: JSON.stringify(data) });
    },
    async getProfile() {
        if (USE_MOCK) {
            // Пытаемся достать сохраненный профиль, если нет — берем начальный
            const saved = localStorage.getItem('mockProfile');
            return saved ? JSON.parse(saved) : MOCKS.me;
        }
        return request('/users/me');
    },
    async updateProfile(data) {
        if (USE_MOCK) {
            const current = await this.getProfile();
            const updatedProfile = { ...current, ...data };
            MOCKS.me = updatedProfile;
            localStorage.setItem('mockProfile', JSON.stringify(updatedProfile));
            return updatedProfile;
        }
        return request('/users/me', { method: 'PATCH', body: JSON.stringify(data) });
    },
    async getGroups() {
        if (USE_MOCK) return mockGroups;
        return request('/groups');
    },
    async createGroup(data) {
        if (USE_MOCK) {
            const newGroup = { group_id: 'mock-' + Date.now(), ...data, is_active: true };
            mockGroups.push(newGroup);
            saveToStorage();
            return newGroup;
        }
        return request('/groups', { method: 'POST', body: JSON.stringify(data) });
    },

    async getBrowseGroups() {
        if (USE_MOCK) {
            // Возвращаем все группы + добавим пару новых для примера
            return [
                ...mockGroups,
                { group_id: '3', name: 'Mathematics 201', description: 'Advanced Calculus', is_active: true },
                { group_id: '4', name: 'Physics Lab', description: 'Experimental Physics', is_active: true },
                { group_id: '5', name: 'English Literature', description: 'Classic Literature Analysis', is_active: false }
            ];
        }
        return request('/groups/browse');
    },
    async joinGroup(groupId) {
        if (USE_MOCK) {
            // Проверяем, не состоим ли уже
            const alreadyJoined = mockGroups.some(g => g.group_id === groupId);
            if (alreadyJoined) {
                throw new Error('Already a member of this group');
            }
            
            // Находим группу и добавляем в мой список
            const allGroups = await this.getBrowseGroups();
            const groupToJoin = allGroups.find(g => g.group_id === groupId);
            
            if (groupToJoin) {
                mockGroups.push({ ...groupToJoin });
                const notif = {
                    id: 'n-' + Date.now(),
                    type: 'group',
                    title: 'Joined Group',
                    message: `You successfully joined "${groupToJoin.name}"`,
                    timestamp: new Date().toISOString(),
                    read: false
                };
                mockNotifications.unshift(notif);
                localStorage.setItem('mockNotifications', JSON.stringify(mockNotifications));
                return { success: true };
            }
            
            throw new Error('Group not found');
        }
        return request(`/groups/${groupId}/join`, { method: 'POST' });
    },

    async leaveGroup(groupId) {
        if (USE_MOCK) {
            // Проверяем, состоим ли мы в этой группе
            const groupIndex = mockGroups.findIndex(g => g.group_id === groupId);
            
            if (groupIndex === -1) {
                throw new Error('You are not a member of this group');
            }
            // Удаляем группу из списка
            mockGroups.splice(groupIndex, 1);
            saveToStorage();
            addNotification('group', 'Left Group', 'You have successfully left the group.');
            
            return { success: true };
        }
        return request(`/groups/${groupId}/leave`, { method: 'DELETE' });
    },

    async getQueues(groupId) {
        if (USE_MOCK) return mockQueues[groupId] || [];
        return request(`/groups/${groupId}/queues`);
    },
    async createQueue(groupId, data) {
        if (USE_MOCK) {
            const newQueue = { queue_id: 'q-' + Date.now(), group_id: groupId, ...data, is_active: true, participants: [], swap_requests: [] };
            if (!mockQueues[groupId]) mockQueues[groupId] = [];
            mockQueues[groupId].push(newQueue);
            saveToStorage();
            return newQueue;
        }
        return request(`/groups/${groupId}/queues`, { method: 'POST', body: JSON.stringify(data) });
    },
    async joinQueue(groupId, queueId) {
        if (USE_MOCK) {
            const queues = mockQueues[groupId];
            if (!queues) throw new Error('Group not found');
            const queue = queues.find(q => q.queue_id === queueId);
            if (!queue) throw new Error('Queue not found');

            queue.participants = queue.participants || [];
            const currentUser = MOCKS.me;
            if (queue.participants.some(p => p.user_id === currentUser.user_id)) throw new Error('Already joined');

            queue.participants.push({
                user_id: currentUser.user_id,
                name: `${currentUser.first_name} ${currentUser.last_name}`,
                position: queue.participants.length + 1,
                status: 'waiting',
                joined_at: new Date().toISOString()
            });
            addNotification('queue', 'Joined Queue', `You joined "${queue.title}" successfully.`);
            saveToStorage();
            return { success: true };
        }
        return request(`/groups/${groupId}/queues/${queueId}/join`, { method: 'POST' });
    },
    async getSwapRequests(groupId, queueId) {
        if (USE_MOCK) {
            const queue = mockQueues[groupId]?.find(q => q.queue_id === queueId);
            return queue ? queue.swap_requests : [];
        }
        return request(`/groups/${groupId}/queues/${queueId}/swaps`);
    },
    async requestSwap(groupId, queueId, targetUserId) {
        if (USE_MOCK) {
            const queue = mockQueues[groupId]?.find(q => q.queue_id === queueId);
            if (!queue) throw new Error('Queue not found');
            const existing = queue.swap_requests.find(r => r.initiator === 'uuid-1' && r.status === 'pending');
            if (existing) throw new Error('Pending request exists');

            const newReq = {
                request_id: 'req-' + Date.now(), initiator: 'uuid-1', target: targetUserId,
                status: 'pending', created_at: new Date().toISOString()
            };
            queue.swap_requests.push(newReq);
            addNotification('swap', 'Swap Requested', 'Your swap request has been sent. Waiting for confirmation.');
            saveToStorage();
            return newReq;
        }
        return request(`/groups/${groupId}/queues/${queueId}/swaps`, { method: 'POST', body: JSON.stringify({ target_user_id: targetUserId }) });
    },
    async respondSwap(groupId, queueId, requestId, accept) {
        if (USE_MOCK) {
            const queue = mockQueues[groupId]?.find(q => q.queue_id === queueId);
            const reqIndex = queue.swap_requests.findIndex(r => r.request_id === requestId);
            if (reqIndex === -1) throw new Error('Request not found');

            const req = queue.swap_requests[reqIndex];
            if (accept) {
                const p1 = queue.participants.find(p => p.user_id === req.initiator);
                const p2 = queue.participants.find(p => p.user_id === req.target);
                if (p1 && p2) {
                    const tempPos = p1.position;
                    p1.position = p2.position;
                    p2.position = tempPos;
                }
            }
            addNotification('swap', 'Swap Accepted', 'Your swap request was accepted. Positions updated.');
            queue.swap_requests.splice(reqIndex, 1);
            saveToStorage();
            return { success: true };
        }
        return request(`/groups/${groupId}/queues/${queueId}/swaps/${requestId}`, { method: 'PATCH', body: JSON.stringify({ status: accept ? 'accepted' : 'rejected' }) });
    },
    
    async getNotifications() {
        if (USE_MOCK) return mockNotifications;
        return request('/notifications');
    },
    
    async clearNotifications() {
        if (USE_MOCK) {
            mockNotifications = [];
            localStorage.setItem('mockNotifications', JSON.stringify(mockNotifications));
            return { success: true };
        }
        return request('/notifications/clear', { method: 'DELETE' });
    },
    
    async markAsRead(id) {
        if (USE_MOCK) {
            const n = mockNotifications.find(n => n.id === id);
            if (n) n.read = true;
            localStorage.setItem('mockNotifications', JSON.stringify(mockNotifications));
            return { success: true };
        }
        return request(`/notifications/${id}/read`, { method: 'PATCH' });
    },

    async leaveQueue(groupId, queueId) {
        if (USE_MOCK) {
            const queues = mockQueues[groupId];
            if (!queues) throw new Error('Group not found');
            const queue = queues.find(q => q.queue_id === queueId);
            if (!queue) throw new Error('Queue not found');
    
            const currentUser = MOCKS.me;
            const participantIndex = queue.participants?.findIndex(p => p.user_id === currentUser.user_id);
            
            if (participantIndex === -1) {
                throw new Error('You are not in this queue');
            }
    
            // Удаляем участника
            queue.participants.splice(participantIndex, 1);
            
            // Обновляем позиции остальных
            queue.participants.forEach((p, index) => {
                p.position = index + 1;
            });
            
            saveToStorage();
            return { success: true };
        }
        return request(`/groups/${groupId}/queues/${queueId}/leave`, { method: 'DELETE' });
    }
};

// === УВЕДОМЛЕНИЯ (MOCK) ===
const savedNotifs = localStorage.getItem('mockNotifications');
let mockNotifications = savedNotifs ? JSON.parse(savedNotifs) : [];

function addNotification(type, title, message) {
    const notif = {
        id: 'n-' + Date.now(),
        type,
        title,
        message,
        timestamp: new Date().toISOString(),
        read: false
    };
    mockNotifications.unshift(notif);
    localStorage.setItem('mockNotifications', JSON.stringify(mockNotifications));
}
