package com.persoff68.fatodo.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.service.exception.PermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ItemServiceClient itemServiceClient;

    public void checkThreadsAdminPermission(Collection<CommentThread> threadCollection) {
        Multimap<CommentThreadType, CommentThread> threadMultimap = threadCollection.stream()
                .collect(Multimaps.toMultimap(
                        CommentThread::getType,
                        Function.identity(),
                        HashMultimap::create
                ));
        threadMultimap.keySet().forEach(key -> checkThreadsAdminPermission(key, threadMultimap.get(key)));
    }

    public void checkThreadReadPermission(CommentThread thread) {
        CommentThreadType type = thread.getType();
        switch (type) {
            case GROUP -> checkGroupReadPermission(thread);
            case ITEM -> checkItemReadPermission(thread);
            default -> throw new PermissionException();
        }
    }

    public void checkCommentPermission(UUID userId, Comment comment) {
        CommentThread thread = comment.getThread();
        checkThreadReadPermission(thread);
        checkOwnComment(userId, comment);
    }

    public void checkReactionPermission(UUID userId, Comment comment) {
        CommentThread thread = comment.getThread();
        checkThreadReadPermission(thread);
        checkNotOwnComment(userId, comment);
    }

    private void checkThreadsAdminPermission(CommentThreadType type, Collection<CommentThread> threadCollection) {
        switch (type) {
            case GROUP -> checkGroupsAdminPermission(threadCollection);
            case ITEM -> checkItemsAdminPermission(threadCollection);
            default -> throw new PermissionException();
        }
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

    private void checkGroupReadPermission(CommentThread thread) {
        UUID groupId = thread.getTargetId();
        boolean hasPermission = itemServiceClient.canReadGroup(groupId);
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

    private void checkGroupsAdminPermission(Collection<CommentThread> threadCollection) {
        List<UUID> groupIdList = threadCollection.stream()
                .map(CommentThread::getTargetId)
                .collect(Collectors.toList());
        boolean hasPermission = itemServiceClient.canAdminGroups(groupIdList);
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

    private void checkItemReadPermission(CommentThread thread) {
        UUID itemId = thread.getTargetId();
        boolean hasPermission = itemServiceClient.canReadItem(itemId);
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

    private void checkItemsAdminPermission(Collection<CommentThread> threadCollection) {
        List<UUID> itemIdList = threadCollection.stream()
                .map(CommentThread::getTargetId)
                .collect(Collectors.toList());
        boolean hasPermission = itemServiceClient.canAdminItems(itemIdList);
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

}
