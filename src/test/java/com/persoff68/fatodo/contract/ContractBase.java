package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.builder.TestReaction;
import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.repository.ReactionRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMessageVerifier
@Transactional
class ContractBase {
    private static final UUID USER_ID = UUID.fromString("8f9a7cae-73c8-4ad6-b135-5bd109b51d2e");
    private static final UUID PARENT_ID = UUID.fromString("06a8e075-1781-4ba9-8096-1d5777b8b09e");
    private static final UUID TARGET_ID = UUID.fromString("b73e8418-ff4a-472b-893d-4e248ae93797");
    private static final UUID COMMENT_ID_1 = UUID.fromString("6796a82a-93c6-4fdf-bf5d-2da77ce2c338");
    private static final UUID COMMENT_ID_2 = UUID.fromString("6520f3e6-0a7f-4c32-b6f8-ba5ae3ed0bd1");

    @Autowired
    WebApplicationContext context;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ReactionRepository reactionRepository;
    @Autowired
    EntityManager entityManager;

    @MockBean
    ItemServiceClient itemServiceClient;
    @MockBean
    WsServiceClient wsServiceClient;
    @MockBean
    EventServiceClient eventServiceClient;

    @BeforeEach
    void setup() {
        reactionRepository.deleteAll();
        commentRepository.deleteAll();
        threadRepository.deleteAll();

        RestAssuredMockMvc.webAppContextSetup(context);

        CommentThread thread1 = createCommentThread(PARENT_ID, TARGET_ID);
        Comment comment1 = createComment(COMMENT_ID_1, thread1, null, USER_ID);
        Comment comment2 = createComment(COMMENT_ID_2, thread1, comment1, UUID.randomUUID());
        createReaction(comment2.getId(), USER_ID);

        doNothing().when(wsServiceClient).sendCommentNewEvent(any());
        doNothing().when(wsServiceClient).sendCommentUpdateEvent(any());
        doNothing().when(eventServiceClient).addCommentEvent(any());

        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);
        when(itemServiceClient.getAllowedGroupIds(any(), any())).thenReturn(List.of(PARENT_ID, TARGET_ID));
        when(itemServiceClient.getAllowedItemIds(any(), any())).thenReturn(List.of(PARENT_ID, TARGET_ID));
    }

    private CommentThread createCommentThread(UUID parentId, UUID targetId) {
        CommentThread thread = TestCommentThread.defaultBuilder()
                .parentId(parentId)
                .targetId(targetId)
                .type(CommentThreadType.GROUP)
                .build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private Comment createComment(UUID id, CommentThread thread, Comment reference, UUID userId) {
        Comment comment = TestComment.defaultBuilder()
                .id(id)
                .thread(thread)
                .reference(reference)
                .userId(userId)
                .build().toParent();
        entityManager.merge(comment);
        return comment;
    }

    private void createReaction(UUID commentId, UUID userId) {
        Reaction reaction = TestReaction.defaultBuilder()
                .commentId(commentId)
                .userId(userId)
                .type(ReactionType.LIKE)
                .build()
                .toParent();
        reactionRepository.saveAndFlush(reaction);
    }

}
