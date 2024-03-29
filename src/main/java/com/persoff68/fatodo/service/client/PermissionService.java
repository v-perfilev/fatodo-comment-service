package com.persoff68.fatodo.service.client;

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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ItemServiceClient itemServiceClient;

    public List<UUID> getThreadUserIds(CommentThread thread) {
        CommentThreadType type = thread.getType();
        if (type.equals(CommentThreadType.GROUP)) {
            return itemServiceClient.getUserIdsByGroupId(thread.getTargetId());
        } else {
            return itemServiceClient.getUserIdsByItemId(thread.getTargetId());
        }
    }

    public List<CommentThread> filterAllowedThreads(String permission, List<CommentThread> threadList) {
        List<UUID> groupTargetIdList = threadList.stream()
                .filter(t -> t.getType().equals(CommentThreadType.GROUP))
                .map(CommentThread::getTargetId)
                .toList();
        List<UUID> itemTargetIdList = threadList.stream()
                .filter(t -> t.getType().equals(CommentThreadType.ITEM))
                .map(CommentThread::getTargetId)
                .toList();

        List<UUID> allowedGroupIdList = itemServiceClient.getAllowedGroupIds(permission, groupTargetIdList);
        List<UUID> allowedItemIdList = itemServiceClient.getAllowedItemIds(permission, itemTargetIdList);
        List<UUID> allowedTargetIdList = Stream.concat(allowedGroupIdList.stream(),
                allowedItemIdList.stream()).toList();

        return threadList.stream()
                .filter(thread -> allowedTargetIdList.contains(thread.getTargetId()))
                .toList();
    }

    public void checkThreadsPermission(String permission, Collection<CommentThread> threadCollection) {
        Multimap<CommentThreadType, CommentThread> threadMultimap = threadCollection.stream()
                .collect(Multimaps.toMultimap(
                        CommentThread::getType,
                        Function.identity(),
                        HashMultimap::create
                ));
        threadMultimap.keySet().forEach(key -> checkSameTypeThreadsPermission(key, permission,
                threadMultimap.get(key)));
    }

    public void checkThreadPermission(String permission, CommentThread thread) {
        CommentThreadType type = thread.getType();
        UUID targetId = thread.getTargetId();
        List<UUID> targetIdList = Collections.singletonList(targetId);
        switch (type) {
            case GROUP -> checkGroupsPermission(permission, targetIdList);
            case ITEM -> checkItemsPermission(permission, targetIdList);
            default -> throw new PermissionException();
        }
    }

    public void checkCommentPermission(UUID userId, Comment comment) {
        checkThreadPermission("READ", comment.getThread());
        checkOwnComment(userId, comment);
    }

    public void checkReactionPermission(UUID userId, Comment comment) {
        checkThreadPermission("READ", comment.getThread());
        checkNotOwnComment(userId, comment);
    }

    private void checkSameTypeThreadsPermission(CommentThreadType type,
                                                String permission,
                                                Collection<CommentThread> threadCollection) {
        List<UUID> targetIdList = threadCollection.stream().map(CommentThread::getTargetId).toList();
        switch (type) {
            case GROUP -> checkGroupsPermission(permission, targetIdList);
            case ITEM -> checkItemsPermission(permission, targetIdList);
            default -> throw new PermissionException();
        }
    }

    private void checkGroupsPermission(String permission, List<UUID> groupIdList) {
        boolean hasPermission = itemServiceClient.hasGroupsPermission(permission, groupIdList);
        if (!hasPermission) {
            throw new PermissionException();
        }
    }

    private void checkItemsPermission(String permission, List<UUID> itemIdList) {
        boolean hasPermission = itemServiceClient.hasItemsPermission(permission, itemIdList);
        if (!hasPermission) {
            throw new PermissionException();
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

}
