package com.persoff68.fatodo.service;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentThreadService {

    private final CommentThreadRepository commentThreadRepository;
    private final PermissionService permissionService;
    private final ItemServiceClient itemServiceClient;

    public CommentThread getByTargetIdOrCreate(UUID targetId) {
        try {
            CommentThread thread = getByTargetId(targetId);
            permissionService.checkThreadPermission(thread);
            return thread;
        } catch (ModelNotFoundException e) {
            CommentThreadType type = getTypeByTargetId(targetId);
            CommentThread threadToCreate = CommentThread.of(targetId, type);
            permissionService.checkThreadPermission(threadToCreate);
            return commentThreadRepository.save(threadToCreate);
        }
    }

    public CommentThread getByTargetId(UUID targetId) {
        return commentThreadRepository.findByTargetId(targetId)
                .orElseThrow(ModelNotFoundException::new);
    }

    private CommentThreadType getTypeByTargetId(UUID targetId) {
        boolean isGroup = itemServiceClient.isGroup(targetId);
        if (isGroup) {
            return CommentThreadType.GROUP;
        }
        boolean isItem = itemServiceClient.isItem(targetId);
        if (isItem) {
            return CommentThreadType.ITEM;
        }
        throw new ModelNotFoundException();
    }

}
