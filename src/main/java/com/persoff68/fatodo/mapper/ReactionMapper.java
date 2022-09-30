package com.persoff68.fatodo.mapper;

import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.dto.ReactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReactionMapper {

    @Mapping(target = "parentId", source = "comment.thread.parentId")
    @Mapping(target = "targetId", source = "comment.thread.targetId")
    @Mapping(target = "commentId", source = "comment.id")
    ReactionDTO pojoToDTO(Reaction reaction);

}
