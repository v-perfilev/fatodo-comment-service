package com.persoff68.fatodo.web.kafka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.config.util.KafkaUtils;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.service.CommentService;
import com.persoff68.fatodo.service.ReactionService;
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
class WsProducerIT {

    private static final String TARGET_ID = "357a2a99-7b7e-4336-9cd7-18f2cf73fab9";
    private static final String USER_ID_1 = "3c300277-b5ea-48d1-80db-ead620cf5846";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    CommentService commentService;
    @Autowired
    ReactionService reactionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;

    @MockBean
    ItemServiceClient itemServiceClient;
    @SpyBean
    WsServiceClient wsServiceClient;

    private ConcurrentMessageListenerContainer<String, WsEventDTO> wsContainer;
    private BlockingQueue<ConsumerRecord<String, WsEventDTO>> wsRecords;

    CommentThread thread;

    @BeforeEach
    void setup() {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);

        thread = createCommentThread(TARGET_ID);

        startWsConsumer();
    }

    @AfterEach
    void cleanup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        stopWsConsumer();
    }

    @Test
    void testSendCommentNewEvent() throws Exception {
        commentService.add(UUID.fromString(USER_ID_1), UUID.fromString(TARGET_ID), "comment", null);

        ConsumerRecord<String, WsEventDTO> record = wsRecords.poll(5, TimeUnit.SECONDS);

        assertThat(wsServiceClient).isInstanceOf(WsProducer.class);
        assertThat(record).isNotNull();
        verify(wsServiceClient).sendEvent(any());
    }

    private void startWsConsumer() {
        JavaType javaType = objectMapper.getTypeFactory().constructType(WsEventDTO.class);
        ConcurrentKafkaListenerContainerFactory<String, WsEventDTO> stringContainerFactory =
                KafkaUtils.buildJsonContainerFactory(embeddedKafkaBroker.getBrokersAsString(),
                        "test", "earliest", javaType);
        wsContainer = stringContainerFactory.createContainer("ws");
        wsRecords = new LinkedBlockingQueue<>();
        wsContainer.setupMessageListener((MessageListener<String, WsEventDTO>) wsRecords::add);
        wsContainer.start();
        ContainerTestUtils.waitForAssignment(wsContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private void stopWsConsumer() {
        wsContainer.stop();
    }


    private CommentThread createCommentThread(String targetId) {
        CommentThread thread =
                TestCommentThread.defaultBuilder()
                        .targetId(UUID.fromString(targetId))
                        .type(CommentThreadType.GROUP).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

}
