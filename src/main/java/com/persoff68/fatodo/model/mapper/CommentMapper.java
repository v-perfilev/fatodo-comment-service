package com.persoff68.fatodo.model.mapper;

import com.persoff68.fatodo.model.AbstractAuditingModel;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.PageableList;
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

    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "children", ignore = true)
    abstract CommentDTO defaultPojoToDTO(Comment comment);

    public CommentDTO pojoToDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentThread thread = comment.getThread();
        UUID threadId = thread != null ? thread.getId() : null;

        Comment parent = comment.getParent();
        UUID parentId = parent != null ? parent.getId() : null;

        List<ReactionDTO> reactionList = getReactionList(comment);

        PageableList<CommentDTO> children = getChildrenList(comment);

        CommentDTO dto = defaultPojoToDTO(comment);
        dto.setThreadId(threadId);
        dto.setParentId(parentId);
        dto.setReactions(reactionList);
        dto.setChildren(children);
        return dto;
    }

    public ReactionsDTO pojoToReactionsDTO(Comment comment) {
        CommentThread thread = comment.getThread();
        UUID threadId = thread != null ? thread.getId() : null;

        List<ReactionDTO> reactionList = getReactionList(comment);

        ReactionsDTO dto = new ReactionsDTO();
        dto.setThreadId(threadId);
        dto.setCommentId(comment.getId());
        dto.setReactions(reactionList);
        return dto;
    }

    private List<ReactionDTO> getReactionList(Comment comment) {
        return comment.getReactions() != null
                ? comment.getReactions().stream()
                .map(reactionMapper::pojoToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    private PageableList<CommentDTO> getChildrenList(Comment comment) {
        PageableList<CommentDTO> children;
        if (comment.getChildren() != null) {
            List<Comment> childrenList = comment.getChildren();
            List<CommentDTO> childrenDTOList = childrenList.stream()
                    .sorted(Comparator.comparing(AbstractAuditingModel::getCreatedAt))
                    .limit(DEFAULT_CHILDREN_SIZE)
                    .map(this::pojoToDTO)
                    .collect(Collectors.toList());
            children = PageableList.of(childrenDTOList, childrenList.size());
        } else {
            children = PageableList.empty();
        }
        return children;
    }

}
