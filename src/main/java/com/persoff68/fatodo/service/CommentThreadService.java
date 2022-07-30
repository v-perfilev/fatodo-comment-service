package com.persoff68.fatodo.service;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.CommentThreadInfo;
import com.persoff68.fatodo.model.ReadStatus;
import com.persoff68.fatodo.model.TypeAndParent;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.service.client.PermissionService;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentThreadService {

    private final PermissionService permissionService;
    private final CommentThreadRepository commentThreadRepository;
    private final ItemServiceClient itemServiceClient;

    public CommentThread getByTargetIdOrCreate(UUID targetId) {
        try {
            CommentThread thread = getByTargetId(targetId);
            permissionService.checkThreadPermission("READ", thread);
            return thread;
        } catch (ModelNotFoundException e) {
            TypeAndParent typeAndParent = getTypeByTargetId(targetId);
            UUID parentId = typeAndParent.getParentId();
            CommentThreadType type = typeAndParent.getType();
            CommentThread threadToCreate = CommentThread.of(parentId, targetId, type);
            permissionService.checkThreadPermission("READ", threadToCreate);
            return commentThreadRepository.save(threadToCreate);
        }
    }

    public CommentThread getByTargetId(UUID targetId) {
        return commentThreadRepository.findByTargetId(targetId)
                .orElseThrow(ModelNotFoundException::new);
    }

    public List<CommentThreadInfo> getInfoByTargetIds(UUID userId, List<UUID> targetIdList) {
        List<CommentThreadInfo> commentThreadInfoList = commentThreadRepository
                .getThreadsAndCountsByTargetIds(userId, targetIdList);
        List<CommentThread> threadList = commentThreadInfoList.stream()
                .map(info -> CommentThread.of(info.getParentId(), info.getTargetId(), info.getType()))
                .toList();
        permissionService.checkThreadsPermission("READ", threadList);
        return commentThreadInfoList;
    }

    @Transactional
    public void deleteAllByParentId(UUID parentId) {
        List<CommentThread> threadList = commentThreadRepository.findByParentId(parentId);
        if (!threadList.isEmpty()) {
            permissionService.checkThreadsPermission("ADMIN", threadList);
            commentThreadRepository.deleteAll(threadList);
        }
    }

    @Transactional
    public void deleteByTargetId(UUID targetId) {
        Optional<CommentThread> threadOptional = commentThreadRepository.findByTargetId(targetId);
        if (threadOptional.isPresent()) {
            CommentThread thread = threadOptional.get();
            permissionService.checkThreadPermission("ADMIN", thread);
            commentThreadRepository.delete(thread);
        }
    }

    @Transactional
    public void refreshReadStatus(UUID userId, UUID targetId) {
        CommentThread thread = commentThreadRepository.findByTargetId(targetId)
                .orElseThrow(ModelNotFoundException::new);
        permissionService.checkThreadPermission("READ", thread);

        List<ReadStatus> readStatusList = thread.getReadStatuses();
        ReadStatus readStatus = readStatusList.stream().filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElse(ReadStatus.of(thread, userId));
        thread.getReadStatuses().add(readStatus);
        readStatus.setLastReadAt(new Date());
        thread.getReadStatuses().add(readStatus);
        commentThreadRepository.save(thread);
    }

    private TypeAndParent getTypeByTargetId(UUID targetId) {
        return itemServiceClient.getTypeAndParent(targetId);
    }

}
