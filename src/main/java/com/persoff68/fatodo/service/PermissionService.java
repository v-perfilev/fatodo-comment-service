package com.persoff68.fatodo.service;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.service.exception.PermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    public void checkThreadPermission(UUID userId, CommentThread thread) {
        // TODO
    }

    public void checkParentPermission(UUID userId, Comment parent) {
        CommentThread thread = parent.getThread();
        checkThreadPermission(userId, thread);
    }

    public void checkCommentPermission(UUID userId, Comment comment) {
        CommentThread thread = comment.getThread();
        checkThreadPermission(userId, thread);
        checkCommentAuthor(userId, comment);
    }

    private void checkCommentAuthor(UUID userId, Comment comment) {
        if (!userId.equals(comment.getUserId())) {
            throw new PermissionException();
        }
    }

}
