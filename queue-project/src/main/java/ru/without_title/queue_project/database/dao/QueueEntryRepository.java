package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.entities.enums.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, UUID> {
    List<QueueEntry> findByQueue_QueueIdOrderByPosition(UUID queueId);

    Optional<QueueEntry> findByQueue_QueueIdAndUser_UserId(UUID queueId, UUID userId);

    long countByQueue(Queue queue);

    boolean existsByQueueAndUser_UserId(Queue queue, UUID userId);

    List<QueueEntry> findByQueue_QueueIdAndStatus(UUID queueId, QueueStatus status);

    @Modifying
    @Query("delete from QueueEntry qe where qe.queue.group.groupId = :groupId and qe.user.userId = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    @Query("select distinct qe.queue.queueId from QueueEntry qe where qe.queue.group.groupId = :groupId and qe.user.userId = :userId")
    List<UUID> findQueueIdsByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    // Reorder positions after a leave/delete without violating unique(queue_id, position).
    // 1) bump affected rows to a high range, 2) shift them down into the freed gap.
    @Modifying
    @Query("""
            update QueueEntry qe
               set qe.position = qe.position + :offset
             where qe.queue.queueId = :queueId
               and qe.position > :fromPos
            """)
    void bumpPositionsAfter(@Param("queueId") UUID queueId, @Param("fromPos") int fromPos, @Param("offset") int offset);

    @Modifying
    @Query("""
            update QueueEntry qe
               set qe.position = qe.position - :offset - 1
             where qe.queue.queueId = :queueId
               and qe.position > (:fromPos + :offset)
            """)
    void shiftBumpedPositionsDown(@Param("queueId") UUID queueId, @Param("fromPos") int fromPos, @Param("offset") int offset);

    @Modifying
    @Query("""
            update QueueEntry qe
               set qe.position = qe.position + :offset
             where qe.queue.queueId = :queueId
            """)
    void bumpAllPositions(@Param("queueId") UUID queueId, @Param("offset") int offset);

    @Modifying
    @Query(value = """
            with ordered as (
                select queue_entry_id,
                       row_number() over (order by position) as rn
                  from queue_entries
                 where queue_id = :queueId
            )
            update queue_entries qe
               set position = ordered.rn
              from ordered
             where qe.queue_entry_id = ordered.queue_entry_id
            """, nativeQuery = true)
    void repackPositions(@Param("queueId") UUID queueId);

    @Query("select coalesce(max(qe.position), 0) from QueueEntry qe where qe.queue.queueId = :queueId")
    int findMaxPosition(@Param("queueId") UUID queueId);

    @Modifying
    @Query("update QueueEntry qe set qe.position = :position, qe.status = :status where qe.queueEntryId = :entryId")
    void setPositionAndStatus(@Param("entryId") UUID entryId, @Param("position") int position, @Param("status") QueueStatus status);

    // Same as bump/shift but excludes a specific entryId (needed for skip).
    @Modifying
    @Query("""
            update QueueEntry qe
               set qe.position = qe.position + :offset
             where qe.queue.queueId = :queueId
               and qe.position > :fromPos
               and qe.queueEntryId <> :entryId
            """)
    void bumpPositionsAfterExcluding(@Param("queueId") UUID queueId, @Param("fromPos") int fromPos, @Param("offset") int offset,
            @Param("entryId") UUID entryId);

    @Modifying
    @Query("""
            update QueueEntry qe
               set qe.position = qe.position - :offset - 1
             where qe.queue.queueId = :queueId
               and qe.position > (:fromPos + :offset)
               and qe.queueEntryId <> :entryId
            """)
    void shiftBumpedPositionsDownExcluding(@Param("queueId") UUID queueId, @Param("fromPos") int fromPos, @Param("offset") int offset,
            @Param("entryId") UUID entryId);
}
