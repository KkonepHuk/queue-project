-- CREATE TYPE queue_status AS ENUM ('WAITING', 'PASSED', 'SKIPPED');
-- CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'READ');
-- CREATE TYPE notification_type AS ENUM ('SYSTEM', 'QUEUE');
-- CREATE TYPE system_role AS ENUM ('SYSTEM_ADMIN', 'USER');
-- CREATE TYPE group_role AS ENUM ('OWNER', 'MODERATOR', 'MEMBER');


--Таблица users
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'SYSTEM_ADMIN')) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Таблица groups
CREATE TABLE groups (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица group_members
CREATE TABLE group_members (
    group_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'MEMBER' CHECK (role IN ('OWNER', 'MODERATOR', 'MEMBER')) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);
-- Таблица queues
CREATE TABLE queues (
    queue_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    reg_open TIMESTAMP NOT NULL,
    reg_close TIMESTAMP NOT NULL,
    max_size INTEGER NOT NULL CHECK (max_size > 0),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (reg_open < reg_close),
    CHECK (reg_open < event_date),
    CHECK (reg_close <= event_date)
);

-- Таблица queue_entries
CREATE TABLE queue_entries (
    queue_entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    queue_id UUID NOT NULL REFERENCES queues(queue_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    position INTEGER NOT NULL CHECK (position >= 1),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'PASSED', 'SKIPPED')) NOT NULL,
    UNIQUE (queue_id, user_id),
    UNIQUE (queue_id, position)
);

-- Таблица notifications
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    queue_id UUID REFERENCES queues(queue_id) ON DELETE CASCADE,
    type VARCHAR(20) CHECK (type IN ('SYSTEM', 'QUEUE')) NOT NULL,
    message TEXT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'READ')) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK(status = 'PENDING' OR sent_at IS NOT NULL),
    CHECK((type = 'QUEUE' AND queue_id IS NOT NULL) OR (type = 'SYSTEM' AND queue_id IS NULL))
);
