package com.persoff68.fatodo.mapper;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThreadInfo;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.CommentInfoDTO;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import com.persoff68.fatodo.model.dto.ReferenceCommentDTO;
import com.persoff68.fatodo.model.dto.ThreadInfoDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

        Comment reference = comment.getReference();
        ReferenceCommentDTO referenceDTO = reference != null ? defaultPojoToReferenceDTO(reference) : null;

        Set<ReactionDTO> reactionList = getReactionSet(comment);

        CommentDTO dto = defaultPojoToDTO(comment);
        dto.setParentId(comment.getThread().getParentId());
        dto.setTargetId(comment.getThread().getTargetId());
        dto.setReference(referenceDTO);
        dto.setReactions(reactionList);
        return dto;
    }

    private Set<ReactionDTO> getReactionSet(Comment comment) {
        return comment.getReactions() != null
                ? comment.getReactions().stream()
                .map(reactionMapper::pojoToDTO)
                .collect(Collectors.toSet())
                : Collections.emptySet();
    }

}
