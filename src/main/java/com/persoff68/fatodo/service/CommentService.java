package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.PageableList;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.service.client.EventService;
import com.persoff68.fatodo.service.client.PermissionService;
import com.persoff68.fatodo.service.client.WsService;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentThreadService threadService;
    private final PermissionService permissionService;
    private final CommentRepository commentRepository;
    private final WsService wsService;
    private final EventService eventService;

    public PageableList<Comment> getAllByTargetIdPageable(UUID targetId, Pageable pageable) {
        CommentThread thread = threadService.getByTargetId(targetId);
        permissionService.checkThreadPermission("READ", thread);
        Page<Comment> commentPage = commentRepository.findAllByThreadId(thread.getId(), pageable);
        return PageableList.of(commentPage.getContent(), commentPage.getTotalElements());
    }

    public List<Comment> getAllAllowedByIds(List<UUID> commentIdList) {
        List<Comment> commentList = commentRepository.findAllByIds(commentIdList);
        List<CommentThread> threadList = commentList.stream().map(Comment::getThread).distinct().toList();
        List<CommentThread> allowedThreadList = permissionService.filterAllowedThreads("READ", threadList);
        return commentList.stream()
                .filter(comment -> allowedThreadList.contains(comment.getThread()))
                .toList();
    }

    @Transactional
    public Comment add(UUID userId, UUID targetId, String text) {
        CommentThread thread = threadService.getByTargetIdOrCreate(targetId);
        Comment comment = Comment.of(userId, thread, text);
        comment = commentRepository.save(comment);

        // WS
        wsService.sendCommentNewEvent(comment);
        // EVENT
        eventService.sendCommentNewEvent(comment);

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
        wsService.sendCommentDeleteEvent(comment);
        // EVENT
        eventService.sendCommentDeleteEvent(comment);
    }

}
