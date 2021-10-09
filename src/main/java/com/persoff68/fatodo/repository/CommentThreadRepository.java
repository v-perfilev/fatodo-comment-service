package com.persoff68.fatodo.repository;

import com.persoff68.fatodo.model.CommentThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentThreadRepository extends JpaRepository<CommentThread, UUID> {

    Optional<CommentThread> findByTargetId(UUID targetId);

    @Query("""
            select distinct t from CommentThread t
            where t.targetId in :threadIds
            """)
    List<CommentThread> findAllByThreadIds(@Param("threadIds") List<UUID> threadIdList);

    @Query("""
            delete from CommentThread t
            where t.id in :ids
            """)
    @Modifying
    void deleteAllByIds(@Param("ids") List<UUID> idList);

}
