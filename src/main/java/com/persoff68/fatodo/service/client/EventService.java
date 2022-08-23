package com.persoff68.fatodo.service.client;

import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.mapper.ReactionMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.EventType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.EventDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
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

    public void sendCommentNewEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        EventDTO eventDTO = new EventDTO(userIdList, EventType.COMMENT_CREATE,
                commentDTO, comment.getUserId());
        eventServiceClient.addEvent(eventDTO);
    }

    public void sendCommentReactionIncomingEvent(Reaction reaction) {
        List<UUID> userIdList = List.of(reaction.getComment().getUserId());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(reaction, reaction.getComment().getThread().getTargetId());
        EventDTO eventDTO = new EventDTO(userIdList, EventType.COMMENT_REACTION_INCOMING,
                reactionDTO, reaction.getUserId());
        eventServiceClient.addEvent(eventDTO);
    }

}
