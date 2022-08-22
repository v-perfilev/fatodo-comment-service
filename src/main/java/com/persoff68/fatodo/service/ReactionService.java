package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.service.client.EventService;
import com.persoff68.fatodo.service.client.PermissionService;
import com.persoff68.fatodo.service.client.WsService;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {

    private final PermissionService permissionService;
    private final CommentRepository commentRepository;
    private final WsService wsService;
    private final EventService eventService;

    public void setLike(UUID userId, UUID messageId) {
        set(userId, messageId, ReactionType.LIKE);
    }

    public void setDislike(UUID userId, UUID messageId) {
        set(userId, messageId, ReactionType.DISLIKE);
    }

    public void remove(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkReactionPermission(userId, comment);


        comment.getReactions().stream()
                .filter(reaction -> reaction.getUserId().equals(userId))
                .findFirst().ifPresent(reaction -> {
                    comment.getReactions().remove(reaction);
                    commentRepository.save(comment);

                    // WS
                    reaction.setType(ReactionType.NONE);
                    wsService.sendCommentReactionEvent(reaction, comment);
                    wsService.sendCommentReactionIncomingEvent(reaction, comment);

                    // EVENT
                    eventService.sendCommentReactionEvent(userId, comment, null);
                });
    }

    protected void set(UUID userId, UUID commentId, ReactionType type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkReactionPermission(userId, comment);

        Optional<Reaction> reactionOptional = comment.getReactions().stream()
                .filter(reaction -> reaction.getUserId().equals(userId)).findFirst();

        Reaction reaction;
        if (reactionOptional.isPresent()) {
            reaction = reactionOptional.get();
            reaction.setType(type);
            reaction.setDate(new Date());
        } else {
            reaction = Reaction.of(comment, userId, type);
            comment.getReactions().add(reaction);
        }
        commentRepository.save(comment);

        // WS
        wsService.sendCommentReactionEvent(reaction, comment);
        wsService.sendCommentReactionIncomingEvent(reaction, comment);
        // EVENT
        eventService.sendCommentReactionEvent(userId, comment, type);
    }

}
