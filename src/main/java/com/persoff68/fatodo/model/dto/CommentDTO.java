package com.persoff68.fatodo.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CommentDTO {
    private UUID threadId;
    private UUID parentId;
    private UUID userId;
    private String text;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private List<ReactionDTO> reactions;

    private List<CommentDTO> children;
}
