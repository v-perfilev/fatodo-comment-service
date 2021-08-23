package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReferenceCommentDTO;
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
                          ReferenceCommentDTO reference,
                          List<ReactionDTO> reactions) {
        super();
        super.setId(id);
        super.setThreadId(threadId);
        super.setUserId(userId);
        super.setText(text);
        super.setDeleted(isDeleted);
        super.setReference(reference);
        super.setReactions(reactions);
    }

    public static TestCommentDTOBuilder defaultBuilder() {
        return TestCommentDTO.builder()
                .text(DEFAULT_VALUE)
                .threadId(UUID.randomUUID())
                .userId(UUID.randomUUID());
    }

    public CommentDTO toParent() {
        CommentDTO dto = new CommentDTO();
        dto.setId(getId());
        dto.setThreadId(getThreadId());
        dto.setUserId(getUserId());
        dto.setText(getText());
        dto.setDeleted(isDeleted());
        dto.setReference(getReference());
        dto.setReactions(getReactions());
        return dto;
    }

}
