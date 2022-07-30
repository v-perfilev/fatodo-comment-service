package com.persoff68.fatodo.repository;

import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.CommentThreadInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentThreadRepository extends JpaRepository<CommentThread, UUID> {

    @Query(value = """
            select id as idBytes, parent_id as parentIdBytes, target_id as targetIdBytes, type, 
            (select count(*) from ftd_comment 
                where thread_id = ftd_comment_thread.id) as count,
            (select count(*) from ftd_comment 
                left join ftd_comment_read_status on ftd_comment.thread_id = ftd_comment_read_status.thread_id
                where ftd_comment.thread_id = ftd_comment_thread.id
                and ftd_comment_read_status.user_id is null
                or (ftd_comment_read_status.user_id = :userId 
                    and ftd_comment.created_at > ftd_comment_read_status.last_read_at)) as unread
            from ftd_comment_thread
            where target_id in :targetIds
            """, nativeQuery = true)
    List<CommentThreadInfo> getThreadsAndCountsByTargetIds(@Param("userId") UUID userId,
                                                           @Param("targetIds") List<UUID> targetIds);

    List<CommentThread> findByParentId(UUID parentId);

    Optional<CommentThread> findByTargetId(UUID targetId);

}
