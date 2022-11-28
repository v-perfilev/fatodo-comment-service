package com.persoff68.fatodo.web.kafka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.config.util.KafkaUtils;
import com.persoff68.fatodo.model.TypeAndParent;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.event.EventDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.service.CommentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "kafka.bootstrapAddress=localhost:9092",
        "kafka.groupId=test",
        "kafka.partitions=1",
        "kafka.autoOffsetResetConfig=earliest"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class EventProducerIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    CommentService commentService;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;

    @MockBean
    ItemServiceClient itemServiceClient;
    @SpyBean
    EventServiceClient eventServiceClient;

    private ConcurrentMessageListenerContainer<String, EventDTO> eventContainer;
    private BlockingQueue<ConsumerRecord<String, EventDTO>> eventRecords;

    @BeforeEach
    void setup() {
        TypeAndParent typeAndParent = new TypeAndParent(CommentThreadType.ITEM, UUID.randomUUID());
        when(itemServiceClient.getTypeAndParent(any())).thenReturn(typeAndParent);
        when(itemServiceClient.hasItemsPermission(any(), any())).thenReturn(true);

        startEventConsumer();
    }

    @AfterEach
    void cleanup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        stopEventConsumer();
    }

    @Test
    void testAddEvent_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        commentService.add(userId, targetId, "test");

        ConsumerRecord<String, EventDTO> record = eventRecords.poll(5, TimeUnit.SECONDS);

        assertThat(eventServiceClient).isInstanceOf(EventProducer.class);
        assertThat(record).isNotNull();
        verify(eventServiceClient).addEvent(any());
    }

    private void startEventConsumer() {
        JavaType javaType = objectMapper.getTypeFactory().constructType(EventDTO.class);
        ConcurrentKafkaListenerContainerFactory<String, EventDTO> containerFactory =
                KafkaUtils.buildJsonContainerFactory(embeddedKafkaBroker.getBrokersAsString(),
                        "test", "earliest", javaType);
        eventContainer = containerFactory.createContainer("event");
        eventRecords = new LinkedBlockingQueue<>();
        eventContainer.setupMessageListener((MessageListener<String, EventDTO>) eventRecords::add);
        eventContainer.start();
        ContainerTestUtils.waitForAssignment(eventContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private void stopEventConsumer() {
        eventContainer.stop();
    }

}
