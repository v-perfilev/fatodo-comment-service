package com.persoff68.fatodo.service.ws;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import com.persoff68.fatodo.model.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Async
public class WsService {

    private final WsServiceClient wsServiceClient;
    private final ItemServiceClient itemServiceClient;
    private final CommentMapper commentMapper;

    public void sendCommentNewEvent(Comment comment) {
        List<UUID> userIdList = getUserIdsForComment(comment);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        wsServiceClient.sendCommentNewEvent(eventDTO);
    }

    public void sendCommentUpdateEvent(Comment comment) {
        List<UUID> userIdList = getUserIdsForComment(comment);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        wsServiceClient.sendCommentUpdateEvent(eventDTO);
    }

    public void sendCommentReactionEvent(Comment comment) {
        List<UUID> userIdList = getUserIdsForComment(comment);
        ReactionsDTO reactionsDTO = commentMapper.pojoToReactionsDTO(comment);
        WsEventDTO<ReactionsDTO> eventDTO = new WsEventDTO<>(userIdList, reactionsDTO);
        wsServiceClient.sendReactionsEvent(eventDTO);
    }

    private List<UUID> getUserIdsForComment(Comment comment) {
        CommentThread thread = comment.getThread();
        CommentThreadType type = thread.getType();
        if (type.equals(CommentThreadType.GROUP)) {
            return itemServiceClient.getGroupUserIdsById(thread.getTargetId());
        } else {
            return itemServiceClient.getItemUserIdsById(thread.getTargetId());
        }
    }

}
