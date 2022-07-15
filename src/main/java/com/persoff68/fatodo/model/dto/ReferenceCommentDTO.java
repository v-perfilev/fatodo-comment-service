package com.persoff68.fatodo.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceCommentDTO extends AbstractAuditingDTO {

    private UUID userId;

}
