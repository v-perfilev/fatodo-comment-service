package com.persoff68.fatodo.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ThreadInfoDTO {

    private UUID targetId;

    private int count;

    private int unread;

}
