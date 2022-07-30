package com.persoff68.fatodo.model.dto;

import com.persoff68.fatodo.model.constant.CommentThreadType;
import lombok.Data;

import java.util.UUID;

@Data
public class ThreadInfoDTO {

    private UUID parentId;

    private UUID targetId;

    private CommentThreadType type;

    private int count;

    private int unread;

}
