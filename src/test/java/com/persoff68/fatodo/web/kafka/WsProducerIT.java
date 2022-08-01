package com.persoff68.fatodo.web.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.config.util.KafkaUtils;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.repository.ReactionRepository;
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
    private static final String USER_ID_2 = "a762e074-0c26-4a3e-9495-44ccb2baf85c";

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
    @Autowired
    ReactionRepository reactionRepository;

    @MockBean
    ItemServiceClient itemServiceClient;
    @SpyBean
    WsServiceClient wsServiceClient;

    private ConcurrentMessageListenerContainer<String, String> wsContainer;
    private BlockingQueue<ConsumerRecord<String, String>> wsRecords;

    CommentThread thread;
    Comment comment;

    @BeforeEach
    void setup() {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);

        thread = createCommentThread(TARGET_ID);
        comment = createComment(thread, null, USER_ID_1);

        startWsConsumer();
    }

    @AfterEach
    void cleanup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();
        reactionRepository.deleteAll();

        stopWsConsumer();
    }

    @Test
    void testSendCommentNewEvent() throws Exception {
        commentService.add(UUID.fromString(USER_ID_1), UUID.fromString(TARGET_ID), "comment", null);

        ConsumerRecord<String, String> record = wsRecords.poll(5, TimeUnit.SECONDS);

        assertThat(wsServiceClient).isInstanceOf(WsProducer.class);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("new");
        verify(wsServiceClient).sendCommentNewEvent(any());
    }

    @Test
    void testSendCommentUpdateEvent() throws Exception {
        commentService.edit(UUID.fromString(USER_ID_1), comment.getId(), "updated-comment");

        ConsumerRecord<String, String> record = wsRecords.poll(5, TimeUnit.SECONDS);

        assertThat(wsServiceClient).isInstanceOf(WsProducer.class);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("update");
        verify(wsServiceClient).sendCommentUpdateEvent(any());
    }

    @Test
    void testSendReactionsEvent() throws Exception {
        reactionService.setLike(UUID.fromString(USER_ID_2), comment.getId());

        ConsumerRecord<String, String> record = wsRecords.poll(5, TimeUnit.SECONDS);

        assertThat(wsServiceClient).isInstanceOf(WsProducer.class);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("reactions");
        verify(wsServiceClient).sendReactionsEvent(any());
    }

    private void startWsConsumer() {
        ConcurrentKafkaListenerContainerFactory<String, String> stringContainerFactory =
                KafkaUtils.buildStringContainerFactory(embeddedKafkaBroker.getBrokersAsString(), "test", "earliest");
        wsContainer = stringContainerFactory.createContainer("ws_comment");
        wsRecords = new LinkedBlockingQueue<>();
        wsContainer.setupMessageListener((MessageListener<String, String>) wsRecords::add);
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

    private Comment createComment(CommentThread thread, Comment reference, String userId) {
        Comment comment = TestComment.defaultBuilder().thread(thread).reference(reference)
                .userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.saveAndFlush(comment);
    }
}
