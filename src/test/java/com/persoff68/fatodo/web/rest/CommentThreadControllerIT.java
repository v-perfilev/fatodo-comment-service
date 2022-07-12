package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.builder.TestReaction;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.Reaction;
import com.persoff68.fatodo.model.constant.CommentThreadType;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
class CommentThreadControllerIT {
    private static final String ENDPOINT = "/api/threads";

    private static final String USER_ID = "3c300277-b5ea-48d1-80db-ead620cf5846";

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

    CommentThread thread1;
    CommentThread thread2;
    Comment comment1;
    Comment comment2;
    Comment comment3;
    Reaction reaction1;

    @BeforeEach
    void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        thread1 = createCommentThread();
        comment1 = createComment(thread1, null, USER_ID);
        comment2 = createComment(thread1, comment1, USER_ID);
        reaction1 = createReaction(comment1, USER_ID);

        thread2 = createCommentThreadWithParentId(thread1.getParentId());
        comment3 = createComment(thread2, null, USER_ID);

        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID)
    void testDeleteAllByParentId_ok() throws Exception {
        String url = ENDPOINT + "/" + thread1.getParentId() + "/parent";
        mvc.perform(delete(url))
                .andExpect(status().isOk());
        List<CommentThread> threadList = threadRepository.findAll();
        List<Comment> commentList = commentRepository.findAll();
        List<Reaction> reactionList = reactionRepository.findAll();
        assertThat(threadList).isEmpty();
        assertThat(commentList).isEmpty();
        assertThat(reactionList).isEmpty();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID)
    void testDeleteAllByParentId_forbidden() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/" + thread1.getParentId() + "/parent";
        mvc.perform(delete(url))
                .andExpect(status().isForbidden());
        List<CommentThread> threadList = threadRepository.findAll();
        assertThat(threadList).isNotEmpty();
    }

    @Test
    @WithAnonymousUser
    void testDeleteAllByParentId_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + thread1.getParentId() + "/parent";
        mvc.perform(delete(url))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID)
    void testDeleteByTargetId_ok() throws Exception {
        String url = ENDPOINT + "/" + thread2.getTargetId() + "/target";
        mvc.perform(delete(url))
                .andExpect(status().isOk());
        List<CommentThread> threadList = threadRepository.findAll();
        List<Comment> commentList = commentRepository.findAll();
        List<Reaction> reactionList = reactionRepository.findAll();
        assertThat(threadList).hasSize(1);
        assertThat(commentList).hasSize(2);
        assertThat(reactionList).hasSize(1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID)
    void testDeleteByTargetId_forbidden() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/" + thread2.getTargetId() + "/target";
        mvc.perform(delete(url))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void testDeleteByTargetId_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + thread2.getTargetId() + "/target";
        mvc.perform(delete(url))
                .andExpect(status().isUnauthorized());
    }

    private CommentThread createCommentThread() {
        CommentThread thread = TestCommentThread.defaultBuilder().type(CommentThreadType.GROUP).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private CommentThread createCommentThreadWithParentId(UUID parentId) {
        CommentThread thread = TestCommentThread.defaultBuilder()
                .parentId(parentId).type(CommentThreadType.GROUP).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private Comment createComment(CommentThread thread, Comment reference, String userId) {
        Comment comment = TestComment.defaultBuilder().thread(thread).reference(reference)
                .userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.saveAndFlush(comment);
    }

    private Reaction createReaction(Comment comment, String userId) {
        Reaction reaction = TestReaction.defaultBuilder().commentId(comment.getId())
                .type(ReactionType.LIKE)
                .userId(UUID.fromString(userId)).build().toParent();
        return reactionRepository.saveAndFlush(reaction);
    }

}
