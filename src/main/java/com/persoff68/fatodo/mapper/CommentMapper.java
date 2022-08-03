package com.persoff68.fatodo.mapper;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.CommentThreadInfo;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.CommentInfoDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.ReferenceCommentDTO;
import com.persoff68.fatodo.model.dto.ThreadInfoDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class CommentMapper {

    @Autowired
    private ReactionMapper reactionMapper;

    @Mapping(target = "reactions", ignore = true)
    abstract CommentDTO defaultPojoToDTO(Comment comment);

    abstract ReferenceCommentDTO defaultPojoToReferenceDTO(Comment comment);

    public abstract CommentInfoDTO pojoToInfoDTO(Comment comment);

    public abstract ThreadInfoDTO threadInfoToDTO(CommentThreadInfo commentThreadInfo);

    public CommentDTO pojoToDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentThread thread = comment.getThread();
        UUID targetId = thread != null ? thread.getTargetId() : null;

        Comment reference = comment.getReference();
        ReferenceCommentDTO referenceDTO = reference != null ? defaultPojoToReferenceDTO(reference) : null;

        List<ReactionDTO> reactionList = getReactionList(comment);

        CommentDTO dto = defaultPojoToDTO(comment);
        dto.setTargetId(targetId);
        dto.setReference(referenceDTO);
        dto.setReactions(reactionList);
        return dto;
    }

    public ReactionsDTO pojoToReactionsDTO(Comment comment) {
        CommentThread thread = comment.getThread();
        UUID targetId = thread != null ? thread.getTargetId() : null;

        List<ReactionDTO> reactionList = getReactionList(comment);

        ReactionsDTO dto = new ReactionsDTO();
        dto.setTargetId(targetId);
        dto.setCommentId(comment.getId());
        dto.setReactions(reactionList);
        return dto;
    }

    private List<ReactionDTO> getReactionList(Comment comment) {
        return comment.getReactions() != null
                ? comment.getReactions().stream()
                .map(reactionMapper::pojoToDTO)
                .toList()
                : Collections.emptyList();
    }

}
