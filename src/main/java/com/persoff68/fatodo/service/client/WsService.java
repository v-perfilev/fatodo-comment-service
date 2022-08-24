package com.persoff68.fatodo.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.mapper.ReactionMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.WsEventType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.event.WsEventDTO;
import com.persoff68.fatodo.service.exception.ModelInvalidException;
import lombok.RequiredArgsConstructor;
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
    private final ReactionMapper reactionMapper;
    private final ObjectMapper objectMapper;

    public void sendCommentNewEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        String payload = serialize(commentDTO);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_CREATE, payload, comment.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentUpdateEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        String payload = serialize(commentDTO);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_UPDATE, payload, comment.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentReactionEvent(Reaction reaction) {
        List<UUID> userIdList = permissionService.getThreadUserIds(reaction.getComment().getThread());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(
                reaction,
                reaction.getComment().getThread().getParentId(),
                reaction.getComment().getThread().getTargetId());
        String payload = serialize(reactionDTO);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_REACTION, payload, reaction.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentReactionIncomingEvent(Reaction reaction) {
        List<UUID> userIdList = List.of(reaction.getComment().getUserId());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(
                reaction,
                reaction.getComment().getThread().getParentId(),
                reaction.getComment().getThread().getTargetId());
        String payload = serialize(reactionDTO);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_REACTION_INCOMING, payload,
                reaction.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ModelInvalidException();
        }
    }

}
