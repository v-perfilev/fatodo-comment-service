package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import lombok.Builder;

import java.util.UUID;

public class TestComment extends Comment {
    private static final String DEFAULT_VALUE = "test_value";

    @Builder
    public TestComment(UUID id,
                       CommentThread thread,
                       UUID userId,
                       String text,
                       boolean isDeleted) {
        super();
        super.setId(id);
        super.setThread(thread);
        super.setUserId(userId);
        super.setText(text);
        super.setDeleted(isDeleted);
    }

    public static TestCommentBuilder defaultBuilder() {
        return TestComment.builder()
                .text(DEFAULT_VALUE)
                .userId(UUID.randomUUID());
    }

    public Comment toParent() {
        Comment comment = new Comment();
        comment.setId(getId());
        comment.setThread(getThread());
        comment.setUserId(getUserId());
        comment.setText(getText());
        comment.setDeleted(isDeleted());
        return comment;
    }

}
