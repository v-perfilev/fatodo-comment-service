package com.persoff68.fatodo.model.mapper;

import com.persoff68.fatodo.model.AbstractAuditingModel;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class CommentMapper {
    private static final int DEFAULT_CHILDREN_SIZE = 5;

    @Autowired
    private ReactionMapper reactionMapper;

    @Mapping(target = "forwardedMessage", ignore = true)
    abstract CommentDTO defaultPojoToDTO(Comment comment);

    public CommentDTO pojoToDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentThread thread = comment.getThread();
        UUID threadId = thread != null ? thread.getId() : null;

        Comment parent = comment.getParent();
        UUID parentId = parent != null ? parent.getId() : null;

        List<ReactionDTO> reactionDTOList = comment.getReactions() != null
                ? comment.getReactions().stream()
                .map(reactionMapper::pojoToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<CommentDTO> childrenDTOList = comment.getChildren() != null
                ? comment.getChildren().stream()
                .sorted(Comparator.comparing(AbstractAuditingModel::getCreatedAt))
                .limit(DEFAULT_CHILDREN_SIZE)
                .map(this::pojoToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        CommentDTO dto = defaultPojoToDTO(comment);
        dto.setThreadId(threadId);
        dto.setParentId(parentId);
        dto.setReactions(reactionDTOList);
        dto.setChildren(childrenDTOList);
        return dto;
    }

    public ReactionsDTO pojoToReactionsDTO(Comment comment) {
        CommentThread thread = comment.getThread();
        UUID threadId = thread != null ? thread.getId() : null;

        List<ReactionDTO> reactionDTOList = comment.getReactions() != null
                ? comment.getReactions().stream()
                .map(reactionMapper::pojoToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        ReactionsDTO dto = new ReactionsDTO();
        dto.setThreadId(threadId);
        dto.setCommentId(comment.getId());
        dto.setReactions(reactionDTOList);
        return dto;
    }

}
