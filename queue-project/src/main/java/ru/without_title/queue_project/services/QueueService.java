package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.entities.enums.QueueStatus;
import ru.without_title.queue_project.dto.request.QueueRequest;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.dao.*;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final GroupRepository groupRepository; // Предполагаем, что он есть
    private final UserRepository userRepository; // Предполагаем, что он есть
    private final GroupMemberRepository groupMemberRepository;

    public QueueService(QueueRepository queueRepository, QueueEntryRepository queueEntryRepository,
                        GroupRepository groupRepository, UserRepository userRepository,
                        GroupMemberRepository groupMemberRepository) {
        this.queueRepository = queueRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public List<Queue> getQueuesByGroupId(UUID groupId) {
        return queueRepository.findByGroup_GroupId(groupId);
    }

    public Queue createQueue(UUID groupId, QueueRequest request, String userEmail) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        var creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        validateQueueDates(request);

        Queue queue = new Queue();
        queue.setGroup(group);
        queue.setCreatedBy(creator);
        queue.setTitle(request.title());
        queue.setDescription(request.description());
        queue.setEventDate(request.eventDate());
        queue.setRandomQueue(request.randomQueue());
        queue.setIsActive(true);
        queue.setCreatedAt(LocalDateTime.now());

        boolean randomQueue = request.randomQueue();
        if (randomQueue) {
            queue.setRegOpen(null);
            queue.setRegClose(null);
            queue.setMaxSize(groupMemberRepository.countByGroup_GroupId(groupId));
        } else {
            queue.setRegOpen(request.regOpen());
            queue.setRegClose(request.regClose());
            queue.setMaxSize(request.maxSize());
        }

        Queue savedQueue = queueRepository.save(queue);

        if (randomQueue) {
            createRandomEntries(savedQueue);
        }

        return savedQueue;
    }

    public Queue getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Очередь не найдена"));
    }

    public Queue updateQueue(UUID queueId, QueueRequest request) {
        Queue queue = getQueueById(queueId);
        validateQueueDates(request);
        queue.setTitle(request.title());
        queue.setDescription(request.description());
        queue.setEventDate(request.eventDate());
        queue.setMaxSize(request.maxSize());
        queue.setRandomQueue(request.randomQueue());

        boolean randomQueue = request.randomQueue();
        queue.setRandomQueue(randomQueue);
        if (randomQueue) {
            queue.setRegOpen(null);
            queue.setRegClose(null);
        } else {
            queue.setRegOpen(request.regOpen());
            queue.setRegClose(request.regClose());
        }

        return queueRepository.save(queue);
    }

    public void deleteQueue(UUID queueId, String requesterEmail, boolean requesterIsAdmin) {
        Queue queue = getQueueById(queueId);
        requireCreatorOrOwner(queue, requesterEmail, requesterIsAdmin);
        queueRepository.delete(queue);
    }

    public void closeQueue(UUID queueId, String requesterEmail, boolean requesterIsAdmin) {
        Queue queue = getQueueById(queueId);
        requireCreatorOrOwner(queue, requesterEmail, requesterIsAdmin);
        queue.setIsActive(false);
        queueRepository.save(queue);
    }

    private void validateQueueDates(QueueRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request"
            );
        }

        LocalDateTime eventDate = request.eventDate();

        if (eventDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event date must be provided"
            );
        }

        boolean randomQueue = Boolean.TRUE.equals(request.randomQueue());
        if (randomQueue) {
            return;
        }

        LocalDateTime regOpen = request.regOpen();
        LocalDateTime regClose = request.regClose();

        if (regOpen == null || regClose == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dates must be provided"
            );
        }

        if (!regOpen.isBefore(regClose)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "regOpen must be before regClose"
            );
        }

        if (!regOpen.isBefore(eventDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "regOpen must be before eventDate"
            );
        }

        if (regClose.isAfter(eventDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "regClose must be on/before eventDate"
            );
        }
    }

    private void createRandomEntries(Queue queue) {

        UUID groupId = queue.getGroup().getGroupId();

        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);

        List<GroupMember> shuffled = new ArrayList<>(members);
        Collections.shuffle(shuffled);

        int position = 1;

        for (GroupMember member : shuffled) {
            QueueEntry entry = new QueueEntry();
            entry.setQueue(queue);
            entry.setUser(member.getUser());
            entry.setPosition(position++);
            entry.setStatus(QueueStatus.WAITING);

            queueEntryRepository.save(entry);
        }
    }

    private void requireCreatorOrOwner(Queue queue, String requesterEmail, boolean requesterIsAdmin) {
        if (requesterIsAdmin) return;
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        boolean isCreator = queue.getCreatedBy() != null && queue.getCreatedBy().getUserId().equals(requester.getUserId());
        if (isCreator) return;

        var groupId = queue.getGroup().getGroupId();
        var member = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, requester.getUserId()).orElse(null);
        boolean isOwnerOrMod = member != null && (member.getRole() == GroupRole.OWNER || member.getRole() == GroupRole.MODERATOR);
        if (!isOwnerOrMod) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only queue creator, group owner, or moderator can perform this action");
        }
    }
}
