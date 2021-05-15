package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.ReactionId;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.ReactionRepository;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final PermissionService permissionService;
    private final EntityManager entityManager;
    private final WsService wsService;

    public void setLike(UUID userId, UUID messageId) {
        set(userId, messageId, ReactionType.LIKE);
    }

    public void setDislike(UUID userId, UUID messageId) {
        set(userId, messageId, ReactionType.DISLIKE);
    }

    public void remove(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        CommentThread thread = comment.getThread();
        permissionService.checkThreadPermission(userId, thread);

        ReactionId id = new ReactionId(commentId, userId);
        reactionRepository.findById(id).ifPresent(reactionRepository::delete);
        reactionRepository.flush();

        entityManager.refresh(comment);

        // WS
//        wsService.sendMessageReactionEvent(message);
    }

    protected void set(UUID userId, UUID commentId, ReactionType type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ModelNotFoundException::new);
        CommentThread thread = comment.getThread();
        permissionService.checkThreadPermission(userId, thread);

        ReactionId id = new ReactionId(commentId, userId);
        Reaction reaction = reactionRepository.findById(id)
                .orElse(new Reaction(commentId, userId, type));
        reaction.setType(type);
        reactionRepository.saveAndFlush(reaction);
        entityManager.refresh(comment);

        // WS
//        wsService.sendMessageReactionEvent(comment);
    }

}
