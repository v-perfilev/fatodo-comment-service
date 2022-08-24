package com.persoff68.fatodo.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.mapper.ReactionMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.EventType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.event.EventDTO;
import com.persoff68.fatodo.service.exception.ModelInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventServiceClient eventServiceClient;
    private final PermissionService permissionService;
    private final CommentMapper commentMapper;
    private final ReactionMapper reactionMapper;
    private final ObjectMapper objectMapper;

    public void sendCommentNewEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        String payload = serialize(commentDTO);
        EventDTO eventDTO = new EventDTO(userIdList, EventType.COMMENT_CREATE, payload, comment.getUserId());
        eventServiceClient.addEvent(eventDTO);
    }

    public void sendCommentReactionIncomingEvent(Reaction reaction) {
        List<UUID> userIdList = List.of(reaction.getComment().getUserId());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(
                reaction,
                reaction.getComment().getThread().getParentId(),
                reaction.getComment().getThread().getTargetId());
        String payload = serialize(reactionDTO);
        EventDTO eventDTO = new EventDTO(userIdList, EventType.COMMENT_REACTION_INCOMING, payload,
                reaction.getUserId());
        eventServiceClient.addEvent(eventDTO);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ModelInvalidException();
        }
    }

}
