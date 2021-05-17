package com.persoff68.fatodo.service;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import com.persoff68.fatodo.model.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WsService {

    private final WsServiceClient wsServiceClient;
    private final ItemServiceClient itemServiceClient;
    private final CommentMapper commentMapper;

    public void sendCommentNewEvent(Comment comment) {
        List<UUID> userIdList = null;
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        wsServiceClient.sendCommentNewEvent(eventDTO);
    }

    public void sendChatUpdateEvent(Comment comment) {
        List<UUID> userIdList = null;
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO<CommentDTO> eventDTO = new WsEventDTO<>(userIdList, commentDTO);
        wsServiceClient.sendCommentUpdateEvent(eventDTO);
    }

    public void sendCommentReactionEvent(Comment comment) {
        List<UUID> userIdList = null;
        ReactionsDTO reactionsDTO = commentMapper.pojoToReactionsDTO(comment);
        WsEventDTO<ReactionsDTO> eventDTO = new WsEventDTO<>(userIdList, reactionsDTO);
        wsServiceClient.sendReactionsEvent(eventDTO);
    }

}
