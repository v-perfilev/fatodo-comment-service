package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

public class TestCommentDTO extends CommentDTO {
    private static final String DEFAULT_VALUE = "test_value";

    @Builder
    public TestCommentDTO(UUID id,
                          UUID threadId,
                          UUID userId,
                          String text,
                          boolean isDeleted,
                          UUID parentId,
                          List<ReactionDTO> reactions,
                          List<CommentDTO> children) {
        super();
        super.setId(id);
        super.setThreadId(threadId);
        super.setUserId(userId);
        super.setText(text);
        super.setDeleted(isDeleted);
        super.setParentId(parentId);
        super.setReactions(reactions);
        super.setChildren(children);
    }

    public static TestCommentDTOBuilder defaultBuilder() {
        return TestCommentDTO.builder()
                .text(DEFAULT_VALUE)
                .userId(UUID.randomUUID());
    }

    public CommentDTO toParent() {
        CommentDTO dto = new CommentDTO();
        dto.setId(getId());
        dto.setThreadId(getThreadId());
        dto.setUserId(getUserId());
        dto.setText(getText());
        dto.setDeleted(isDeleted());
        dto.setParentId(getParentId());
        dto.setReactions(getReactions());
        dto.setChildren(getChildren());
        return dto;
    }

}
