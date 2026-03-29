# Frontend Concept Document 

**Project:** Queue Service  
**Version:** 1.0.0   
**Status:** Active Development

---

## 1. Project Overview

### 1.1 Purpose

**Queue Service** is a web application for managing queues within groups. Users can create groups, create queues inside them, join queues, track their position, and swap places with other users.

### 1.2 Core Features

| Feature                   | Description     |
|---------------------------|-----------------|
| **User Authentication**   | Registration, login, profile management                |
| **Group Management**      | Create, join, manage groups with role-based access     |
| **Queue Management**      | Create queues, join/leave, track position in real-time |
| **Entry Management**      | Update entry status (WAITING, PASSED, SKIPPED)         |
| **Notifications**         | System and queue-related notifications                 |

### 1.3 Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+) |
| **Backend** | Java (Spring Boot) |
| **Database** | PostgreSQL |
| **Communication** | REST API (JSON over HTTP) |
| **Authentication** | JWT Bearer Tokens |

---

## 2. Architecture

### 2.1 System Architecture

```
┌─────────────┐      HTTP/JSON      ┌─────────────┐      JDBC     ┌─────────────┐
│   Frontend  │ ◄─────────────────► │   Backend   │ ◄───────────► │  Database   │
│  (HTML/CSS/ │     Port: 8080      │    (Java)   │    Port: 5433 │ (PostgreSQL)│
│     JS)     │                     │  Spring Boot│               │             │
└─────────────┘                     └─────────────┘               └─────────────┘
```

### 2.2 Key Principles

1. **Separation of Concerns:** Frontend never accesses the database directly
2. **API-First:** All data operations go through REST API
3. **Stateless Authentication:** JWT tokens for session management
4. **Progressive Enhancement:** Core functionality works without JavaScript

### 2.3 Data Flow

```
User Action → Frontend Logic → API Request → Backend → Database
                ↑                                          ↓
                └────────────── Response ◄─────────────────┘
```

---

## 3. File Structure

### 3.1 Proposed Structure (Multi-Page Application)

```
/frontend
│
├── /pages                      # HTML pages
│   ├── index.html              # Authorization/Login
│   ├── register.html           # Registration
│   ├── dashboard.html          # Main application page
│   ├── groups.html             # Group management
│   ├── queues.html             # Queue management
│   └── profile.html            # User profile
│
├── /styles                     # CSS files
│   ├── base.css                # Reset, variables, global styles
│   ├── components.css          # Reusable components (buttons, cards, modals)
│   ├── forms.css               # Form-specific styles
│   ├── layout.css              # Layout and grid systems
│   └── pages/                  # Page-specific styles
│       ├── dashboard.css
│       ├── groups.css
│       └── queues.css
│
├── /scripts                    # JavaScript files
│   ├── app.js                  # Main application logic
│   ├── auth.js                 # Authentication logic
│   ├── api.js                  # API client and requests
│   ├── utils.js                # Helper functions
│   └── pages/                  # Page-specific scripts
│       ├── dashboard.js
│       ├── groups.js
│       └── queues.js
│
├── /assets                     # Static resources
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── /docs                       # Documentation
│   ├── FRONTEND_CONCEPT.md     # This file
│   └── API_NOTES.md            # API integration notes
│
├── /config                     # Configuration files
│   └── config.js               # Environment configuration
│
└── index.html                  # Entry point (redirects to /pages)
```

### 3.2 Structure Notes

> ⚠️ **This is a proposed structure.** The actual structure may vary based on project growth and team decisions.

| Directory | Purpose |
|-----------|---------|
| `/pages` | All HTML pages organized in one location |
| `/styles` | Modular CSS for better maintainability |
| `/scripts` | JavaScript separated by functionality |
| `/assets` | Images, icons, fonts |
| `/docs` | Project documentation |
| `/config` | Environment-specific configuration |

---

## 4. Authentication & Authorization

### 4.1 Authentication Flow

```
┌──────────┐      ┌──────────┐     ┌──────────┐     ┌──────────┐
│  User    │────> │  Login   │────>│ Backend  │────>│  Token   │
│  Input   │      │  Request │     │  Verify  │     │  Storage │
└──────────┘      └──────────┘     └──────────┘     └──────────┘
                                           │
                                           ▼
                                    ┌──────────┐
                                    │ Dashboard│
                                    │  Access  │
                                    └──────────┘
```

### 4.2 Implementation

#### Login Request
```javascript
const response = await fetch('http://localhost:8080/api/v1/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
});

const { token } = await response.json();
localStorage.setItem('authToken', token);
```

#### Protected Request
```javascript
const token = localStorage.getItem('authToken');

const response = await fetch('http://localhost:8080/api/v1/users/me', {
    method: 'GET',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});
```

#### Token Expiration Handling
```javascript
if (response.status === 401) {
    localStorage.removeItem('authToken');
    window.location.href = '/pages/index.html';
}
```

## 5. API Integration

### 5.1 Configuration

```javascript
// config.js
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api/v1',
    USE_MOCK: true,  // true for development without backend
    DEBUG: true
};
```

### 5.2 API Client Pattern

```javascript
// scripts/api.js
class ApiClient {
    constructor(baseURL) {
        this.baseURL = baseURL;
        this.token = localStorage.getItem('authToken');
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.token}`,
            ...options.headers
        };

        const response = await fetch(url, { ...options, headers });
        
        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('authToken');
                window.location.href = '/pages/index.html';
            }
            throw new Error(`API Error: ${response.status}`);
        }

        return response.status === 204 ? null : await response.json();
    }

    // User endpoints
    users = {
        login: (data) => this.request('/users/login', { method: 'POST', body: JSON.stringify(data) }),
        register: (data) => this.request('/users/register', { method: 'POST', body: JSON.stringify(data) }),
        me: () => this.request('/users/me'),
        update: (data) => this.request('/users/me', { method: 'PATCH', body: JSON.stringify(data) }),
        delete: () => this.request('/users/me', { method: 'DELETE' })
    };

    // Group endpoints
    groups = {
        list: () => this.request('/groups'),
        create: (data) => this.request('/groups', { method: 'POST', body: JSON.stringify(data) }),
        get: (id) => this.request(`/groups/${id}`),
        update: (id, data) => this.request(`/groups/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
        delete: (id) => this.request(`/groups/${id}`, { method: 'DELETE' }),
        members: (id) => this.request(`/groups/${id}/members`),
        addMember: (id, data) => this.request(`/groups/${id}/members`, { method: 'POST', body: JSON.stringify(data) })
    };

    // Queue endpoints
    queues = {
        list: (groupId) => this.request(`/groups/${groupId}/queues`),
        create: (groupId, data) => this.request(`/groups/${groupId}/queues`, { method: 'POST', body: JSON.stringify(data) }),
        get: (id) => this.request(`/queues/${id}`),
        join: (id) => this.request(`/queues/${id}/join`, { method: 'POST' }),
        leave: (id) => this.request(`/queues/${id}/leave`, { method: 'DELETE' }),
        entries: (id) => this.request(`/queues/${id}/entries`)
    };

    // Notification endpoints
    notifications = {
        list: () => this.request('/notifications'),
        markRead: (id) => this.request(`/notifications/${id}/read`, { method: 'PATCH' })
    };
}

// Initialize
const api = new ApiClient(CONFIG.API_BASE_URL);
```

### 5.3 Available Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users/register` | Register new user | No |
| POST | `/users/login` | Login | No |
| GET | `/users/me` | Get current user | Yes |
| PATCH | `/users/me` | Update user | Yes |
| DELETE | `/users/me` | Deactivate user | Yes |
| GET | `/groups` | List groups | Yes |
| POST | `/groups` | Create group | Yes |
| GET | `/groups/{id}` | Get group | Yes |
| PATCH | `/groups/{id}` | Update group | Yes |
| DELETE | `/groups/{id}` | Delete group | Yes |
| GET | `/groups/{id}/members` | Get members | Yes |
| POST | `/groups/{id}/members` | Add member | Yes |
| GET | `/groups/{id}/queues` | Get queues | Yes |
| POST | `/groups/{id}/queues` | Create queue | Yes |
| POST | `/queues/{id}/join` | Join queue | Yes |
| DELETE | `/queues/{id}/leave` | Leave queue | Yes |
| GET | `/queues/{id}/entries` | Get entries | Yes |
| GET | `/notifications` | Get notifications | Yes |
| PATCH | `/notifications/{id}/read` | Mark as read | Yes |

### 5.4 Mock Mode

```javascript
// For development without backend
if (CONFIG.USE_MOCK) {
    api.users.me = async () => ({
        user_id: '1',
        email: 'user@example.com',
        first_name: 'John',
        last_name: 'Doe'
    });
    
    api.groups.list = async () => [
        { group_id: '1', name: 'Test Group', description: 'Demo' }
    ];
}
```

---

## 6. Proposed Page Descriptions

### 6.1 Authorization Page (`index.html`)

**Purpose:** User login

**Elements:**
- Email input field
- Password input field
- Login button
- Error message display
- Link to registration page

**Actions:**
- Validate email format
- Send credentials to `/users/login`
- Store JWT token
- Redirect to dashboard on success

### 6.2 Registration Page (`register.html`)

**Purpose:** New user registration

**Elements:**
- Email input
- Password input
- First name input
- Last name input
- Register button
- Link to login page

**Actions:**
- Validate all fields
- Send data to `/users/register`
- Auto-login or redirect to login

### 6.3 Dashboard Page (`dashboard.html`)

**Purpose:** Main application hub

**Elements:**
- User info header
- List of user's groups
- Notifications panel
- Quick actions (create group, etc.)

**Actions:**
- Load user data from `/users/me`
- Load groups from `/groups`
- Load notifications from `/notifications`

### 6.4 Groups Page (`groups.html`)

**Purpose:** Group management

**Elements:**
- Group list/cards
- Create group button
- Group details modal
- Member management

**Actions:**
- CRUD operations for groups
- Add/remove members
- Change member roles

### 6.5 Queues Page (`queues.html`)

**Purpose:** Queue management

**Elements:**
- Queue list within selected group
- Create queue button
- Queue entries list
- Position tracker

**Actions:**
- Create/delete queues
- Join/leave queues
- View entry status (WAITING, PASSED, SKIPPED)

### 6.6 Profile Page (`profile.html`)

**Purpose:** User profile management

**Elements:**
- User information display
- Edit profile form
- Change password form
- Delete account button

**Actions:**
- Update user data via `/users/me` PATCH
- Deactivate account via `/users/me` DELETE

---

## 7. Styling System

### 7.1 Design Philosophy
The frontend uses a component-based styling approach with CSS variables for consistency and maintainability. All visual elements follow a unified design language that can be easily extended as the project grows.

---

**Document Version:** 1.0.0  
**Author:** Frontend Development Team  
**Status:** Active Development