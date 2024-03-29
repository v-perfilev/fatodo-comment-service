package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.builder.TestReaction;
import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.repository.ReactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
class ReactionControllerIT {
    private static final String ENDPOINT = "/api/reaction";

    private static final String USER_ID_1 = "3c300277-b5ea-48d1-80db-ead620cf5846";
    private static final String USER_ID_2 = "357a2a99-7b7e-4336-9cd7-18f2cf73fab9";

    @Autowired
    MockMvc mvc;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ReactionRepository reactionRepository;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemServiceClient itemServiceClient;
    @MockBean
    WsServiceClient wsServiceClient;
    @MockBean
    EventServiceClient eventServiceClient;

    Comment comment1;
    Comment comment2;
    Comment comment3;
    Comment comment4;

    @BeforeEach
    void setup() {
        CommentThread thread1 = createThread();
        comment1 = createComment(thread1, USER_ID_2);
        comment2 = createComment(thread1, USER_ID_1);
        comment3 = createComment(thread1, USER_ID_2);
        createReaction(comment3, USER_ID_1, ReactionType.DISLIKE);

        CommentThread thread2 = createThread();
        comment4 = createComment(thread2, USER_ID_2);

        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);
    }

    @AfterEach
    void cleanup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();
        reactionRepository.deleteAll();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_ok() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists = reactionList.stream()
                .anyMatch(status -> status.getComment().getId().toString().equals(commentId) && status.getUserId().toString().equals(USER_ID_1) && status.getType().equals(ReactionType.LIKE));
        assertThat(reactionExists).isTrue();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_ok_wasDislike() throws Exception {
        String commentId = comment3.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists =
                reactionList.stream().anyMatch(status -> status.getComment().getId().toString().equals(commentId) && status.getUserId().toString().equals(USER_ID_1) && status.getType().equals(ReactionType.LIKE));
        assertThat(reactionExists).isTrue();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_forbidden_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_forbidden_noPermissions() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetLike_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/like";
        mvc.perform(post(url)).andExpect(status().isUnauthorized());
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_ok() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/dislike";
        mvc.perform(post(url)).andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists =
                reactionList.stream().anyMatch(status -> status.getComment().getId().toString().equals(commentId) && status.getUserId().toString().equals(USER_ID_1) && status.getType().equals(ReactionType.DISLIKE));
        assertThat(reactionExists).isTrue();
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_forbidden_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/dislike";
        mvc.perform(post(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_forbidden_noPermissions() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/dislike";
        mvc.perform(post(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/" + commentId + "/dislike";
        mvc.perform(post(url)).andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetDislike_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/" + commentId + "/dislike";
        mvc.perform(post(url)).andExpect(status().isUnauthorized());
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_ok() throws Exception {
        String commentId = comment3.getId().toString();
        String url = ENDPOINT + "/" + commentId;
        mvc.perform(delete(url)).andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists =
                reactionList.stream().anyMatch(status -> status.getComment().getId().toString().equals(commentId) && status.getUserId().toString().equals(USER_ID_1));
        assertThat(reactionExists).isFalse();
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_forbidden_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/" + commentId;
        mvc.perform(delete(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_forbidden_noPermissions() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/" + commentId;
        mvc.perform(delete(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_badRequest_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/" + commentId;
        mvc.perform(delete(url)).andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetNone_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/" + commentId;
        mvc.perform(delete(url)).andExpect(status().isUnauthorized());
    }


    private CommentThread createThread() {
        CommentThread thread = TestCommentThread.defaultBuilder().build().toParent();
        return threadRepository.save(thread);
    }

    private Comment createComment(CommentThread thread, String userId) {
        Comment comment =
                TestComment.defaultBuilder().thread(thread).userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.save(comment);
    }

    private void createReaction(Comment comment, String userId, ReactionType type) {
        Reaction reaction =
                TestReaction.defaultBuilder().comment(comment).userId(UUID.fromString(userId)).type(type).build().toParent();
        comment.getReactions().add(reaction);
        commentRepository.save(comment);
    }

}
