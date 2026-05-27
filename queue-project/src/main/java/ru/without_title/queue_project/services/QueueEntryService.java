package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.dao.*;
import ru.without_title.queue_project.database.entities.enums.*;
import ru.without_title.queue_project.database.entities.enums.GroupRole;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class QueueEntryService {
    private final QueueEntryRepository entryRepository;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public QueueEntryService(QueueEntryRepository entryRepository, QueueRepository queueRepository, UserRepository userRepository,
            GroupMemberRepository groupMemberRepository) {
        this.entryRepository = entryRepository;
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public void joinQueue(UUID queueId, String userEmail) {
        // 1. Ищем очередь
        var queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Очередь не найдена"));
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        // 2. Проверяем, активна ли она и открыта ли регистрация
        LocalDateTime now = LocalDateTime.now();
        if (!queue.getIsActive() || now.isBefore(queue.getRegOpen()) || now.isAfter(queue.getRegClose())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Регистрация в очередь закрыта или еще не началась");
        }

        // 3. Проверяем лимит мест
        long currentCount = entryRepository.countByQueue(queue);
        if (currentCount >= queue.getMaxSize()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "В очереди больше нет свободных мест");
        }

        if (entryRepository.existsByQueueAndUser_UserId(queue, user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Вы уже записаны в эту очередь");
        }

        // Ensure positions are packed before we choose the next position (prevents duplicates after bulk deletes).
        repackQueuePositions(queueId);

        // 5. Создаем новую запись
        QueueEntry entry = new QueueEntry();
        entry.setQueue(queue);
        entry.setUser(user);
        long packedCount = entryRepository.countByQueue(queue);
        entry.setPosition((int) packedCount + 1); // Позиция = кол-во людей + 1
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinedAt(now);

        entryRepository.save(entry);
    }

    @Transactional
    public void leaveQueue(UUID queueId, String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        QueueEntry entry = entryRepository.findByQueue_QueueIdAndUser_UserId(queueId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Вы не записаны в эту очередь"));

        int removedPos = entry.getPosition() != null ? entry.getPosition() : 0;
        entryRepository.delete(entry);

        // Shift positions down to avoid gaps: 1,2,4 -> 1,2,3.
        // Two-step to avoid violating unique(queue_id, position) during updates.
        if (removedPos > 0) {
            final int OFFSET = 1000000;
            entryRepository.bumpPositionsAfter(queueId, removedPos, OFFSET);
            entryRepository.shiftBumpedPositionsDown(queueId, removedPos, OFFSET);
        }
    }

    public List<QueueEntry> getEntriesByQueueId(UUID queueId) {
        return entryRepository.findByQueue_QueueIdOrderByPosition(queueId);
    }

    @Transactional
    public QueueEntry updateStatus(UUID queueId, UUID userId, QueueStatus status, String requesterEmail, boolean requesterIsAdmin) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        var queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Очередь не найдена"));

        var groupId = queue.getGroup().getGroupId();
        var requesterMember = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, requester.getUserId()).orElse(null);
        boolean requesterIsOwner = requesterMember != null && requesterMember.getRole() == GroupRole.OWNER;

        QueueEntry entry = entryRepository.findByQueue_QueueIdAndUser_UserId(queueId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));

        if (status == QueueStatus.SKIPPED) {
            // Skip rules: the user can skip themselves, or OWNER can skip anyone (admins always can).
            if (!requesterIsAdmin && !requesterIsOwner && !requester.getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Skip is allowed only for the participant or the group owner");
            }
            if (entry.getStatus() != QueueStatus.WAITING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Only WAITING participants can be skipped");
            }
            return skipEntry(queueId, entry);
        }

        if (status == QueueStatus.PASSED) {
            // Passed rules: the user can mark themselves as answered, or OWNER can mark anyone (admins always can).
            if (!requesterIsAdmin && !requesterIsOwner && !requester.getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Marking as PASSED is allowed only for the participant or the group owner");
            }
            entry.setStatus(status);
            return entryRepository.save(entry);
        }

        // Other status changes are "moderation": creator/OWNER/MODERATOR/admin.
        if (!requesterIsAdmin) {
            boolean isCreator = queue.getCreatedBy() != null && queue.getCreatedBy().getUserId().equals(requester.getUserId());
            boolean canManage = isCreator || (requesterMember != null && (requesterMember.getRole() == GroupRole.OWNER || requesterMember.getRole() == GroupRole.MODERATOR));
            if (!canManage) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to manage queue entries");
            }
        }

        entry.setStatus(status);
        return entryRepository.save(entry);
    }

    private QueueEntry skipEntry(UUID queueId, QueueEntry entry) {
        // Move the entry to the end but keep it WAITING (so it can be skipped again later).
        int fromPos = entry.getPosition() != null ? entry.getPosition() : 0;
        if (fromPos <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid queue position");
        }

        // Safe two-step reorder (no transient UNIQUE(queue_id, position) collisions):
        // 1) move skipped entry out of the way, 2) re-pack positions > fromPos, 3) put skipped entry at the end.
        final int OFFSET = 1000000;
        int total = (int) entryRepository.countByQueue(entry.getQueue());
        if (total <= 1) {
            entryRepository.setPositionAndStatus(entry.getQueueEntryId(), fromPos, QueueStatus.WAITING);
        } else {
            // IMPORTANT: must not collide with bumped positions (max bumped is total+OFFSET),
            // so use total+OFFSET+1 as a temporary safe slot.
            entryRepository.setPositionAndStatus(entry.getQueueEntryId(), total + OFFSET + 1, QueueStatus.WAITING);
            entryRepository.bumpPositionsAfterExcluding(queueId, fromPos, OFFSET, entry.getQueueEntryId());
            entryRepository.shiftBumpedPositionsDownExcluding(queueId, fromPos, OFFSET, entry.getQueueEntryId());
            entryRepository.setPositionAndStatus(entry.getQueueEntryId(), total, QueueStatus.WAITING);
        }

        return entryRepository.findById(entry.getQueueEntryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
    }

    private void repackQueuePositions(UUID queueId) {
        // Pack positions to 1..N. Two-step: bump out of range then repack.
        final int OFFSET = 1000000;
        int maxPos = entryRepository.findMaxPosition(queueId);
        if (maxPos <= 1) return;
        entryRepository.bumpAllPositions(queueId, OFFSET);
        entryRepository.repackPositions(queueId);
    }
}
