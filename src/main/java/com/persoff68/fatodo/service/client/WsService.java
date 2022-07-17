package com.persoff68.fatodo.service.client;

import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WsService {

    private final WsServiceClient wsServiceClient;
    private final PermissionService permissionService;
    private final CommentMapper commentMapper;

    public void sendCommentNewEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        sendCommentNewEventAsync(eventDTO);
    }

    public void sendCommentUpdateEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        sendCommentUpdateEventAsync(eventDTO);
    }

    public void sendCommentReactionEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        ReactionsDTO reactionsDTO = commentMapper.pojoToReactionsDTO(comment);
        WsEventDTO<ReactionsDTO> eventDTO = new WsEventDTO<>(userIdList, reactionsDTO);
        sendReactionsEventAsync(eventDTO);
    }

    @Async
    public void sendCommentNewEventAsync(WsEventDTO<CommentDTO> event) {
        wsServiceClient.sendCommentNewEvent(event);
    }

    @Async
    public void sendCommentUpdateEventAsync(WsEventDTO<CommentDTO> event) {
        wsServiceClient.sendCommentUpdateEvent(event);
    }

    @Async
    public void sendReactionsEventAsync(WsEventDTO<ReactionsDTO> event) {
        wsServiceClient.sendReactionsEvent(event);
    }

}
