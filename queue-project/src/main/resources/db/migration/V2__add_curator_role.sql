-- Добавляем роль CURATOR в group_members
ALTER TABLE group_members 
DROP CONSTRAINT group_members_role_check;

ALTER TABLE group_members 
ADD CONSTRAINT group_members_role_check 
CHECK (role IN ('OWNER', 'MODERATOR', 'MEMBER', 'CURATOR'));

-- Индекс для быстрого поиска кураторов по группам
CREATE INDEX IF NOT EXISTS idx_group_members_curator 
ON group_members(group_id, user_id) 
WHERE role = 'CURATOR';
