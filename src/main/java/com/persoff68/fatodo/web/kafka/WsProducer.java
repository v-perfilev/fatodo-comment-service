package com.persoff68.fatodo.web.kafka;

import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.config.annotation.ConditionalOnPropertyNotNull;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnPropertyNotNull(value = "kafka.bootstrapAddress")
public class WsProducer implements WsServiceClient {

    private static final String WS_COMMENT_TOPIC = "ws_comment";

    private final KafkaTemplate<String, WsEventDTO<CommentDTO>> wsEventCommentKafkaTemplate;
    private final KafkaTemplate<String, WsEventDTO<ReactionsDTO>> wsEventReactionsKafkaTemplate;

    public void sendCommentNewEvent(WsEventDTO<CommentDTO> event) {
        wsEventCommentKafkaTemplate.send(WS_COMMENT_TOPIC, "new", event);
    }

    public void sendCommentUpdateEvent(WsEventDTO<CommentDTO> event) {
        wsEventCommentKafkaTemplate.send(WS_COMMENT_TOPIC, "update", event);
    }

    public void sendReactionsEvent(WsEventDTO<ReactionsDTO> event) {
        wsEventReactionsKafkaTemplate.send(WS_COMMENT_TOPIC, "reactions", event);
    }

}
