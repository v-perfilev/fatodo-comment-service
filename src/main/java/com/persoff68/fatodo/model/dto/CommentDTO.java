package com.persoff68.fatodo.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDTO extends AbstractAuditingDTO {

    private UUID parentId;

    private UUID targetId;

    private UUID userId;

    private String text;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private Set<ReactionDTO> reactions;
}
