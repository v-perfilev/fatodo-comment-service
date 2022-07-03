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

    private final KafkaTemplate<String, WsEventDTO<CommentDTO>> wsEventCommentKafkaTemplate;
    private final KafkaTemplate<String, WsEventDTO<ReactionsDTO>> wsEventReactionsKafkaTemplate;

    public void sendCommentNewEvent(WsEventDTO<CommentDTO> event) {
        wsEventCommentKafkaTemplate.send("ws_comment", "new", event);
    }

    public void sendCommentUpdateEvent(WsEventDTO<CommentDTO> event) {
        wsEventCommentKafkaTemplate.send("ws_comment", "update", event);
    }

    public void sendReactionsEvent(WsEventDTO<ReactionsDTO> event) {
        wsEventReactionsKafkaTemplate.send("ws_comment", "reactions", event);
    }

}
