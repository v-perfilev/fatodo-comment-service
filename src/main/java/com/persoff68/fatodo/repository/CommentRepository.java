package com.persoff68.fatodo.repository;

import com.persoff68.fatodo.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
            select distinct c from Comment c
            where c.thread.id = :threadId
            order by c.createdAt desc
            """)
    Page<Comment> findAllByThreadId(@Param("threadId") UUID threadId, Pageable pageable);

    @Query("""
            select distinct c from Comment c
            where c.id in :commentIds
            """)
    List<Comment> findAllByIds(@Param("commentIds") List<UUID> commentIds);

}


