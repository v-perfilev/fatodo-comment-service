package com.persoff68.fatodo.repository;

import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.CommentThreadAndCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentThreadRepository extends JpaRepository<CommentThread, UUID> {

    @Query("""
            select t as thread, size(t.comments) as count from CommentThread t
            where t.targetId in :targetIds        
            """)
    List<CommentThreadAndCount> getThreadsAndCountsByTargetIds(@Param("targetIds") List<UUID> targetIds);

    List<CommentThread> findByParentId(UUID parentId);

    Optional<CommentThread> findByTargetId(UUID targetId);

}
