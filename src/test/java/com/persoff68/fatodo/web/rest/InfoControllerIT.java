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
import com.persoff68.fatodo.model.dto.ThreadInfoDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.crossstore.ChangeSetPersister;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        comment1 = createComment(thread1, USER_ID_1);
        comment2 = createComment(thread1, USER_ID_1);

        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);
        when(itemServiceClient.hasItemsPermission(any(), any())).thenReturn(true);
        when(itemServiceClient.getAllowedGroupIds(eq("READ"), any())).thenReturn(List.of(thread1.getTargetId()));
        when(itemServiceClient.getAllowedItemIds(eq("READ"), any())).thenReturn(List.of(thread2.getTargetId()));
    }

    @AfterEach
    void cleanup() {
        commentRepository.deleteAll();
        threadRepository.deleteAll();
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


    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetInfoByTargetIds_ok_unread() throws Exception {
        String params = String.join(",", thread1.getTargetId().toString(), thread2.getTargetId().toString());
        String url = ENDPOINT + "/thread?ids=" + params;
        ResultActions resultActions = mvc.perform(get(url)).andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, ThreadInfoDTO.class);
        List<ThreadInfoDTO> resultList = objectMapper.readValue(resultString, javaType);
        assertThat(resultList).hasSize(2);
        ThreadInfoDTO threadInfoDTO = resultList.stream().filter(i -> i.getTargetId().equals(thread1.getTargetId()))
                .findFirst().orElseThrow(ChangeSetPersister.NotFoundException::new);
        assertThat(threadInfoDTO.getCount()).isEqualTo(2);
        assertThat(threadInfoDTO.getUnread()).isEqualTo(2);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetInfoByTargetIds_ok_read() throws Exception {
        String refreshUrl = ENDPOINT + "/thread/" + thread1.getTargetId() + "/refresh";
        mvc.perform(put(refreshUrl)).andExpect(status().isOk());

        String params = String.join(",", thread1.getTargetId().toString(), thread2.getTargetId().toString());
        String url = ENDPOINT + "/thread?ids=" + params;
        ResultActions resultActions = mvc.perform(get(url)).andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, ThreadInfoDTO.class);
        List<ThreadInfoDTO> resultList = objectMapper.readValue(resultString, javaType);
        assertThat(resultList).hasSize(2);
        ThreadInfoDTO threadInfoDTO = resultList.stream().filter(i -> i.getTargetId().equals(thread1.getTargetId()))
                .findFirst().orElseThrow(ChangeSetPersister.NotFoundException::new);
        assertThat(threadInfoDTO.getCount()).isEqualTo(2);
        assertThat(threadInfoDTO.getUnread()).isZero();
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetInfoByTargetIds_forbidden() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String params = String.join(",", thread1.getTargetId().toString(), thread2.getTargetId().toString());
        String url = ENDPOINT + "/thread?ids=" + params;
        mvc.perform(get(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void testGetInfoByTargetIds_unauthorized() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String params = String.join(",", thread1.getTargetId().toString(), thread2.getTargetId().toString());
        String url = ENDPOINT + "/thread?ids=" + params;
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testRefresh_ok() throws Exception {
        String url = ENDPOINT + "/thread/" + thread1.getTargetId() + "/refresh";
        mvc.perform(put(url)).andExpect(status().isOk());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testRefresh_notFound() throws Exception {
        String url = ENDPOINT + "/thread/" + UUID.randomUUID() + "/refresh";
        mvc.perform(put(url)).andExpect(status().isNotFound());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testRefresh_forbidden() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/thread/" + thread1.getTargetId() + "/refresh";
        mvc.perform(put(url)).andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void testRefresh_unauthorized() throws Exception {
        String url = ENDPOINT + "/thread/" + thread1.getTargetId() + "/refresh";
        mvc.perform(put(url)).andExpect(status().isUnauthorized());
    }


    private CommentThread createCommentThread(CommentThreadType type) {
        CommentThread thread = TestCommentThread.defaultBuilder().type(type).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private Comment createComment(CommentThread thread, String userId) {
        Comment comment = TestComment.defaultBuilder()
                .thread(thread).userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.saveAndFlush(comment);
    }

}
