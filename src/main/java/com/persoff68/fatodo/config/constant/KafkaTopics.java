package com.persoff68.fatodo.config.constant;

import lombok.Getter;

public enum KafkaTopics {
    EVENT_ADD("event_add"),
    WS_COMMENT("ws_comment");

    @Getter
    private final String value;

    KafkaTopics(String value) {
        this.value = value;
    }

}
