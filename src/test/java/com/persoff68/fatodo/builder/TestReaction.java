package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.ReactionType;
import lombok.Builder;

import java.util.UUID;

public class TestReaction extends Reaction {

    @Builder
    public TestReaction(UUID commentId, UUID userId, ReactionType type) {
        super(commentId, userId, type, null);
    }


    public static TestReactionBuilder defaultBuilder() {
        return TestReaction.builder();
    }

    public Reaction toParent() {
        Reaction reaction = new Reaction();
        reaction.setUserId(getUserId());
        reaction.setCommentId(getCommentId());
        reaction.setType(getType());
        reaction.setTimestamp(getTimestamp());
        return reaction;
    }

}
