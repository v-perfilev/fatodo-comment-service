package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.ReactionType;
import lombok.Builder;

import java.util.UUID;

public class TestReaction extends Reaction {

    @Builder
    public TestReaction(Comment comment, UUID userId, ReactionType type) {
        super();
        super.setComment(comment);
        super.setUserId(userId);
        super.setType(type);
    }


    public static TestReactionBuilder defaultBuilder() {
        return TestReaction.builder();
    }

    public Reaction toParent() {
        Reaction reaction = new Reaction();
        reaction.setUserId(getUserId());
        reaction.setComment(getComment());
        reaction.setType(getType());
        reaction.setDate(getDate());
        return reaction;
    }

}
