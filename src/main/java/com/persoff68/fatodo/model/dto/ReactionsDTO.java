package com.persoff68.fatodo.model.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReactionsDTO {

    private UUID threadId;
    private UUID targetId;
    private UUID commentId;

    private List<ReactionDTO> reactions;

}
