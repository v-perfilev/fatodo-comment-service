package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.PageableList;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.service.client.PermissionService;
import com.persoff68.fatodo.service.exception.ModelInvalidException;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import com.persoff68.fatodo.service.client.WsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentThreadService threadService;
    private final PermissionService permissionService;
    private final CommentRepository commentRepository;
    private final WsService wsService;

    public PageableList<Comment> getAllByTargetIdPageable(UUID targetId, Pageable pageable) {
        CommentThread thread = threadService.getByTargetId(targetId);
        permissionService.checkThreadPermission("READ", thread);
        Page<Comment> commentPage = commentRepository.findAllByThreadId(thread.getId(), pageable);
        return PageableList.of(commentPage.getContent(), commentPage.getTotalElements());
    }

    @Transactional
    public Comment add(UUID userId, UUID targetId, String text, UUID referenceId) {
        Comment comment = referenceId == null
                ? addWithoutReference(userId, targetId, text)
                : addWithReference(userId, targetId, text, referenceId);
        comment = commentRepository.save(comment);

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

        // WS
        wsService.sendCommentUpdateEvent(comment);
    }

    private Comment addWithReference(UUID userId, UUID targetId, String text, UUID referenceId) {
        Comment comment;
        Comment reference = commentRepository.findById(referenceId)
                .orElseThrow(ModelNotFoundException::new);
        CommentThread thread = reference.getThread();
        if (!thread.getTargetId().equals(targetId)) {
            throw new ModelInvalidException();
        }
        permissionService.checkThreadPermission("READ", thread);
        comment = Comment.of(userId, thread, reference, text);
        return comment;
    }

    private Comment addWithoutReference(UUID userId, UUID targetId, String text) {
        Comment comment;
        CommentThread thread = threadService.getByTargetIdOrCreate(targetId);
        comment = Comment.of(userId, thread, text);
        return comment;
    }

}
