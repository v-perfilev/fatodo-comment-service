package com.persoff68.fatodo.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.persoff68.fatodo.config.constant.AppConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDTO extends AbstractAuditingDTO {
    @Serial
    private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

    private UUID threadId;
    private UUID targetId;
    private UUID userId;
    private String text;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private ReferenceCommentDTO reference;

    private List<ReactionDTO> reactions;
}
