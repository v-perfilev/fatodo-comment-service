package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

public class TestCommentThread extends CommentThread {

    @Builder
    public TestCommentThread(UUID id,
                             UUID parentId,
                             UUID targetId,
                             CommentThreadType type,
                             List<Comment> comments) {
        super();
        super.setId(id);
        super.setParentId(parentId);
        super.setTargetId(targetId);
        super.setType(type);
        super.setComments(comments);
    }

    public static TestCommentThreadBuilder defaultBuilder() {
        return TestCommentThread.builder()
                .parentId(UUID.randomUUID())
                .targetId(UUID.randomUUID())
                .type(CommentThreadType.GROUP);
    }

    public CommentThread toParent() {
        CommentThread thread = new CommentThread();
        thread.setId(getId());
        thread.setParentId(getParentId());
        thread.setTargetId(getTargetId());
        thread.setType(getType());
        thread.setComments(getComments());
        return thread;
    }

}
