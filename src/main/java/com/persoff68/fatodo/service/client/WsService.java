package com.persoff68.fatodo.service.client;

import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.mapper.ReactionMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.WsEventType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
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

    public void sendCommentNewEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_CREATE,
                commentDTO, comment.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentUpdateEvent(Comment comment) {
        CommentThread thread = comment.getThread();
        List<UUID> userIdList = permissionService.getThreadUserIds(thread);
        CommentDTO commentDTO = commentMapper.pojoToDTO(comment);
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_UPDATE,
                commentDTO, comment.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentReactionEvent(Reaction reaction) {
        List<UUID> userIdList = permissionService.getThreadUserIds(reaction.getComment().getThread());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(reaction, reaction.getComment().getThread().getTargetId());
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_REACTION,
                reactionDTO, reaction.getUserId());
        wsServiceClient.sendEvent(dto);
    }

    public void sendCommentReactionIncomingEvent(Reaction reaction) {
        List<UUID> userIdList = List.of(reaction.getComment().getUserId());
        ReactionDTO reactionDTO = reactionMapper.pojoToDTO(reaction, reaction.getComment().getThread().getTargetId());
        WsEventDTO dto = new WsEventDTO(userIdList, WsEventType.COMMENT_REACTION_INCOMING,
                reactionDTO, reaction.getUserId());
        wsServiceClient.sendEvent(dto);
    }

}
