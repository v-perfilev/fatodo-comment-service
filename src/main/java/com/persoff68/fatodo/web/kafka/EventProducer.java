package com.persoff68.fatodo.web.kafka;

import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.config.annotation.ConditionalOnPropertyNotNull;
import com.persoff68.fatodo.config.constant.KafkaTopics;
import com.persoff68.fatodo.model.dto.CreateCommentEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnPropertyNotNull(value = "kafka.bootstrapAddress")
public class EventProducer implements EventServiceClient {

    private final KafkaTemplate<String, CreateCommentEventDTO> eventCommentKafkaTemplate;

    @Override
    public void addCommentEvent(CreateCommentEventDTO createCommentEventDTO) {
        eventCommentKafkaTemplate.send(KafkaTopics.EVENT_ADD.getValue(), "comment", createCommentEventDTO);
    }

}
