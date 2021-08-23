package com.persoff68.fatodo.service;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.service.exception.PermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ItemServiceClient itemServiceClient;

    public void checkThreadPermission(CommentThread thread) {
        CommentThreadType type = thread.getType();
        if (type.equals(CommentThreadType.GROUP)) {
            checkGroupPermission(thread);
        } else if (type.equals(CommentThreadType.ITEM)) {
            checkItemPermission(thread);
        } else {
            throw new PermissionException();
        }
    }

    public void checkCommentPermission(UUID userId, Comment comment) {
        CommentThread thread = comment.getThread();
        checkThreadPermission(thread);
        checkOwnComment(userId, comment);
    }

    public void checkReactionPermission(UUID userId, Comment comment) {
        CommentThread thread = comment.getThread();
        checkThreadPermission(thread);
        checkNotOwnComment(userId, comment);
    }

    private void checkOwnComment(UUID userId, Comment comment) {
        if (!userId.equals(comment.getUserId())) {
            throw new PermissionException();
        }
    }

    private void checkNotOwnComment(UUID userId, Comment comment) {
        if (userId.equals(comment.getUserId())) {
            throw new PermissionException();
        }
    }

    private void checkGroupPermission(CommentThread thread) {
        boolean hasPermission = itemServiceClient.canReadGroup(thread.getTargetId());
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

    private void checkItemPermission(CommentThread thread) {
        boolean hasPermission = itemServiceClient.canReadItem(thread.getTargetId());
        if (!hasPermission) {
            throw new PermissionException();
        }
    }


}
