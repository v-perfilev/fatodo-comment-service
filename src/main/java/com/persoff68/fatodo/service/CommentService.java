package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import com.persoff68.fatodo.service.ws.WsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

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

    public Pair<List<Comment>, Long> getAllByTargetIdPageable(UUID targetId, Pageable pageable) {
        CommentThread thread = threadService.getById(targetId);
        permissionService.checkThreadPermission(thread);
        Page<Comment> commentPage = commentRepository.findAllByThreadId(targetId, pageable);
        return Pair.of(commentPage.getContent(), commentPage.getTotalElements());
    }

    @Transactional
    public Comment add(UUID userId, UUID targetId, String text) {
        CommentThread thread = threadService.getByIdOrCreate(targetId);
        Comment comment = Comment.of(userId, thread, text);
        comment = commentRepository.save(comment);

        // WS
        wsService.sendCommentNewEvent(comment);

        return comment;
    }

    @Transactional
    public Comment addWithReference(UUID userId, UUID referenceId, String text) {
        Comment reference = commentRepository.findById(referenceId)
                .orElseThrow(ModelNotFoundException::new);
        CommentThread thread = reference.getThread();

        permissionService.checkThreadPermission(thread);

        Comment comment = Comment.of(userId, thread, reference, text);
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

}
