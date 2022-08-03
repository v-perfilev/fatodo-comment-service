package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestReactionsDTO extends ReactionsDTO {

    @Builder
    TestReactionsDTO(UUID targetId, UUID commentId, List<ReactionDTO> reactions) {
        super();
        super.setTargetId(targetId);
        super.setCommentId(commentId);
        super.setReactions(reactions);
    }

    public static TestReactionsDTOBuilder defaultBuilder() {
        return TestReactionsDTO.builder()
                .targetId(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .reactions(Collections.singletonList(TestReactionDTO.defaultBuilder().build().toParent()));
    }

    public ReactionsDTO toParent() {
        ReactionsDTO dto = new ReactionsDTO();
        dto.setTargetId(getTargetId());
        dto.setCommentId(getCommentId());
        dto.setReactions(getReactions());
        return dto;
    }

}

