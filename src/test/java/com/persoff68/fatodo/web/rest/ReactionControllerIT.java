package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.builder.TestReaction;
import com.persoff68.fatodo.client.GroupServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.ReactionType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.repository.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
public class ReactionControllerIT {
    private static final String ENDPOINT = "/api/reactions";

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
    GroupServiceClient groupServiceClient;

    Comment comment1;
    Comment comment2;
    Comment comment3;
    Comment comment4;

    @BeforeEach
    public void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();
        reactionRepository.deleteAll();

        CommentThread thread1 = createThread();
        comment1 = createComment(thread1, USER_ID_2);
        comment2 = createComment(thread1, USER_ID_1);
        comment3 = createComment(thread1, USER_ID_2);
        createReaction(comment3.getId(), USER_ID_1, ReactionType.DISLIKE);

        CommentThread thread2 = createThread();
        comment4 = createComment(thread2, USER_ID_2);

        when(groupServiceClient.canRead(Collections.singletonList(thread1.getId()))).thenReturn(true);
        when(groupServiceClient.canRead(Collections.singletonList(thread2.getId()))).thenReturn(false);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_ok() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists = reactionList.stream()
                .anyMatch(status -> status.getCommentId().toString().equals(commentId)
                        && status.getUserId().toString().equals(USER_ID_1)
                        && status.getType().equals(ReactionType.LIKE));
        assertThat(reactionExists).isTrue();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_ok_wasDislike() throws Exception {
        String commentId = comment3.getId().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists = reactionList.stream()
                .anyMatch(status -> status.getCommentId().toString().equals(commentId)
                        && status.getUserId().toString().equals(USER_ID_1)
                        && status.getType().equals(ReactionType.LIKE));
        assertThat(reactionExists).isTrue();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_badRequest_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_badRequest_noPermissions() throws Exception {
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetLike_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetLike_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/like/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_ok() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/dislike/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists = reactionList.stream()
                .anyMatch(status -> status.getCommentId().toString().equals(commentId)
                        && status.getUserId().toString().equals(USER_ID_1)
                        && status.getType().equals(ReactionType.DISLIKE));
        assertThat(reactionExists).isTrue();
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_badRequest_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/dislike/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_badRequest_noPermissions() throws Exception {
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/dislike/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetDislike_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/dislike/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetDislike_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/dislike/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_ok() throws Exception {
        String commentId = comment3.getId().toString();
        String url = ENDPOINT + "/none/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isCreated());
        List<Reaction> reactionList = reactionRepository.findAll();
        boolean reactionExists = reactionList.stream()
                .anyMatch(status -> status.getCommentId().toString().equals(commentId)
                        && status.getUserId().toString().equals(USER_ID_1));
        assertThat(reactionExists).isFalse();
    }


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_badRequest_ownMessage() throws Exception {
        String commentId = comment2.getId().toString();
        String url = ENDPOINT + "/none/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_badRequest_noPermissions() throws Exception {
        String commentId = comment4.getId().toString();
        String url = ENDPOINT + "/none/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testSetNone_badRequest_notFound() throws Exception {
        String commentId = UUID.randomUUID().toString();
        String url = ENDPOINT + "/none/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testSetNone_unauthorized() throws Exception {
        String commentId = comment1.getId().toString();
        String url = ENDPOINT + "/none/" + commentId;
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }


    private CommentThread createThread() {
        CommentThread thread = TestCommentThread.defaultBuilder().build().toParent();
        return threadRepository.save(thread);
    }

    private Comment createComment(CommentThread thread, String userId) {
        Comment comment = TestComment.defaultBuilder()
                .thread(thread).userId(UUID.fromString(userId))
                .build().toParent();
        return commentRepository.save(comment);
    }

    private void createReaction(UUID commentId, String userId, ReactionType type) {
        Reaction reaction = TestReaction.defaultBuilder()
                .commentId(commentId).userId(UUID.fromString(userId)).type(type).build().toParent();
        reactionRepository.save(reaction);
    }

}
