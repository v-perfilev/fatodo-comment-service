package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestReactionsDTO extends ReactionsDTO {

    @Builder
    TestReactionsDTO(UUID threadId, UUID commentId, List<ReactionDTO> reactions) {
        super();
        super.setThreadId(threadId);
        super.setCommentId(commentId);
        super.setReactions(reactions);
    }

    public static TestReactionsDTOBuilder defaultBuilder() {
        return TestReactionsDTO.builder()
                .threadId(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .reactions(Collections.singletonList(TestReactionDTO.defaultBuilder().build().toParent()));
    }

    public ReactionsDTO toParent() {
        ReactionsDTO dto = new ReactionsDTO();
        dto.setThreadId(getThreadId());
        dto.setCommentId(getCommentId());
        dto.setReactions(getReactions());
        return dto;
    }

}

