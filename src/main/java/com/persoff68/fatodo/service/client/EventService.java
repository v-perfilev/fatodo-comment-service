package com.persoff68.fatodo.service.client;

import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.model.dto.CreateCommentEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventServiceClient eventServiceClient;
    private final PermissionService permissionService;

    public void sendCommentAddEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        UUID userId = comment.getUserId();
        UUID parentId = thread.getParentId();
        UUID targetId = thread.getTargetId();
        UUID commentId = comment.getId();
        List<UUID> recipientIdList = permissionService.getThreadUserIds(thread);
        recipientIdList.remove(userId);
        CreateCommentEventDTO dto = CreateCommentEventDTO.commentAdd(recipientIdList, userId,
                parentId, targetId, commentId);
        eventServiceClient.addCommentEvent(dto);
    }

    public void sendCommentReactionEvent(UUID userId, Comment comment, ReactionType reaction) {
        CommentThread thread = comment.getThread();
        UUID recipientId = comment.getUserId();
        UUID parentId = thread.getParentId();
        UUID targetId = thread.getTargetId();
        UUID commentId = comment.getId();
        String reactionName = reaction != null ? reaction.name() : null;
        CreateCommentEventDTO dto = CreateCommentEventDTO.commentReaction(recipientId, userId,
                parentId, targetId, commentId, reactionName);
        eventServiceClient.addCommentEvent(dto);
    }

}
