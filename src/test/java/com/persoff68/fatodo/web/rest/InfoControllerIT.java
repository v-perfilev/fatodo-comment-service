package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.CommentInfoDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
class InfoControllerIT {
    private static final String ENDPOINT = "/api/info";

    private static final String USER_ID_1 = "3c300277-b5ea-48d1-80db-ead620cf5846";

    @Autowired
    MockMvc mvc;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemServiceClient itemServiceClient;

    CommentThread thread1;
    CommentThread thread2;
    Comment comment1;
    Comment comment2;

    @BeforeEach
    void setup() {
        thread1 = createCommentThread(CommentThreadType.GROUP);
        thread2 = createCommentThread(CommentThreadType.ITEM);
        comment1 = createComment(thread1, null, USER_ID_1);
        comment2 = createComment(thread1, comment1, USER_ID_1);

        when(itemServiceClient.getAllowedGroupIds(eq("READ"), any())).thenReturn(List.of(thread1.getTargetId()));
        when(itemServiceClient.getAllowedItemIds(eq("READ"), any())).thenReturn(List.of(thread2.getTargetId()));
    }

    @AfterEach
    void cleanup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void getAllCommentInfoByIds_ok() throws Exception {
        String params = String.join(",", comment1.getId().toString(), comment2.getId().toString());
        String url = ENDPOINT + "/comment?ids=" + params;
        ResultActions resultActions = mvc.perform(get(url)).andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentInfoDTO.class);
        List<CommentInfoDTO> resultList = objectMapper.readValue(resultString, javaType);
        assertThat(resultList).hasSize(2);
    }

    @Test
    @WithAnonymousUser
    void getAllCommentInfoByIds_unauthorized() throws Exception {
        String params = String.join(",", comment1.getId().toString(), comment2.getId().toString());
        String url = ENDPOINT + "/comment?ids=" + params;
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
    }


    private CommentThread createCommentThread(CommentThreadType type) {
        CommentThread thread = TestCommentThread.defaultBuilder().type(type).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private Comment createComment(CommentThread thread, Comment reference, String userId) {
        Comment comment =
                TestComment.defaultBuilder().thread(thread).reference(reference).userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.saveAndFlush(comment);
    }

}
