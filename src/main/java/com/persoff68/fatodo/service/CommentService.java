package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.service.exception.ModelInvalidException;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import com.persoff68.fatodo.service.ws.WsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentThreadService threadService;
    private final PermissionService permissionService;
    private final WsService wsService;
    private final EntityManager entityManager;

    public List<Comment> getParentsByTargetIdPageable(UUID targetId, Pageable pageable) {
        CommentThread thread = threadService.getById(targetId);
        permissionService.checkThreadPermission(thread);
        return commentRepository.findParentCommentsByThreadId(targetId, pageable);
    }

    public List<Comment> getChildrenByParentIdPageable(UUID parentId, Pageable pageable) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkParentPermission(parent);
        return commentRepository.findChildCommentsByThreadId(parentId, pageable);
    }

    @Transactional
    public Comment addParent(UUID userId, UUID targetId, String text) {
        CommentThread tread = threadService.getByIdOrCreate(targetId);
        Comment comment = Comment.of(userId, tread, text);
        comment = commentRepository.save(comment);

        // WS
        wsService.sendCommentNewEvent(comment);

        return comment;
    }

    @Transactional
    public Comment addChild(UUID userId, UUID parentId, String text) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(ModelNotFoundException::new);
        if (parent.getParent() != null) {
            throw new ModelInvalidException();
        }
        permissionService.checkParentPermission(parent);

        Comment comment = Comment.of(userId, parent, text);
        comment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(parent);

        // WS
        wsService.sendCommentNewEvent(comment);

        return comment;
    }

    @Transactional
    public Comment edit(UUID userId, UUID commentId, String text) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkCommentPermission(userId, comment);

        comment.setText(text);
        comment = commentRepository.save(comment);

        Comment parent = comment.getParent();
        if (parent != null) {
            entityManager.refresh(parent);
        }

        // WS
        wsService.sendCommentUpdateEvent(comment);

        return comment;
    }

    @Transactional
    public void delete(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkCommentPermission(userId, comment);

        comment.setText(null);
        comment.setDeleted(true);
        commentRepository.save(comment);

        Comment parent = comment.getParent();
        if (parent != null) {
            entityManager.refresh(parent);
        }

        // WS
        wsService.sendCommentUpdateEvent(comment);
    }

}
