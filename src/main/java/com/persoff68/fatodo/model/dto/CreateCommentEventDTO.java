package com.persoff68.fatodo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentEventDTO {

    private EventType type;

    private List<UUID> recipientIds;

    private UUID userId;

    private UUID parentId;

    private UUID targetId;

    private UUID commentId;

    private String reaction;

    public enum EventType {
        COMMENT_ADD,
        COMMENT_REACTION,
    }

    public static CreateCommentEventDTO commentAdd(List<UUID> recipientIds, UUID userId,
                                                   UUID parentId, UUID targetId, UUID commentId) {
        CreateCommentEventDTO dto = new CreateCommentEventDTO();
        dto.setType(EventType.COMMENT_ADD);
        dto.setRecipientIds(recipientIds);
        dto.setUserId(userId);
        dto.setParentId(parentId);
        dto.setTargetId(targetId);
        dto.setCommentId(commentId);
        return dto;
    }

    public static CreateCommentEventDTO commentReaction(UUID recipientId, UUID userId,
                                                        UUID parentId, UUID targetId, UUID commentId, String reaction) {
        CreateCommentEventDTO dto = new CreateCommentEventDTO();
        dto.setType(EventType.COMMENT_REACTION);
        dto.setRecipientIds(Collections.singletonList(recipientId));
        dto.setUserId(userId);
        dto.setParentId(parentId);
        dto.setTargetId(targetId);
        dto.setCommentId(commentId);
        dto.setReaction(reaction);
        return dto;
    }

}
