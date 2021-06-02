package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
public class CommentControllerIT {
    private static final String ENDPOINT = "/api/comments";

    private static final String USER_ID_1 = "3c300277-b5ea-48d1-80db-ead620cf5846";
    private static final String USER_ID_2 = "357a2a99-7b7e-4336-9cd7-18f2cf73fab9";

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
    @MockBean
    WsServiceClient wsServiceClient;

    CommentThread thread1;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    public void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        thread1 = TestCommentThread.defaultBuilder().type(CommentThreadType.GROUP).build().toParent();
        thread1 = threadRepository.saveAndFlush(thread1);

        comment1 = TestComment.defaultBuilder().thread(thread1)
                .userId(UUID.fromString(USER_ID_1)).build().toParent();
        comment1 = commentRepository.saveAndFlush(comment1);

        comment2 = TestComment.defaultBuilder().thread(thread1).parent(comment1)
                .userId(UUID.fromString(USER_ID_1)).build().toParent();
        comment2 = commentRepository.saveAndFlush(comment2);

        comment3 = TestComment.defaultBuilder().thread(thread1)
                .userId(UUID.fromString(USER_ID_2)).build().toParent();
        comment3 = commentRepository.saveAndFlush(comment3);

        doNothing().when(wsServiceClient).sendCommentNewEvent(any());
        doNothing().when(wsServiceClient).sendCommentUpdateEvent(any());

        when(itemServiceClient.canReadGroup(any())).thenReturn(true);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllParentsPageable_ok_withoutParams() throws Exception {
        String url = ENDPOINT + "/" + thread1.getId();
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
        List<CommentDTO> resultDTOList = objectMapper.readValue(resultString, listType);
        assertThat(resultDTOList.size()).isEqualTo(2);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllParentsPageable_ok_withParams() throws Exception {
        String url = ENDPOINT + "/" + thread1.getId() + "?offset=1&size=10";
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
        List<CommentDTO> resultDTOList = objectMapper.readValue(resultString, listType);
        assertThat(resultDTOList.size()).isEqualTo(1);
    }

    @Test
    @WithAnonymousUser
    void testGetAllParentsPageable_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + thread1.getId();
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllChildrenPageable_ok_withoutParams() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId() + "/children";
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
        List<CommentDTO> resultDTOList = objectMapper.readValue(resultString, listType);
        assertThat(resultDTOList.size()).isEqualTo(1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllChildrenPageable_ok_withParams() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId() + "/children?offset=1&size=10";
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
        List<CommentDTO> resultDTOList = objectMapper.readValue(resultString, listType);
        assertThat(resultDTOList.size()).isEqualTo(0);
    }

    @Test
    @WithAnonymousUser
    void testGetAllChildrenPageable_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId() + "/children";
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddParent_ok_existingThread() throws Exception {
        String url = ENDPOINT + "/" + thread1.getId();
        String requestBody = "test_text";
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(requestBody);
        assertThat(dto.getUserId().toString()).isEqualTo(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddParent_ok_newThread() throws Exception {
        UUID newThreadId = UUID.randomUUID();
        when(itemServiceClient.isGroup(newThreadId)).thenReturn(false);
        when(itemServiceClient.isItem(newThreadId)).thenReturn(true);
        when(itemServiceClient.canReadItem(newThreadId)).thenReturn(true);
        String url = ENDPOINT + "/" + newThreadId;
        String requestBody = "test_text";
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(requestBody);
        assertThat(dto.getUserId().toString()).isEqualTo(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddParent_badRequest_wrongPermission() throws Exception {
        UUID newThreadId = UUID.randomUUID();
        when(itemServiceClient.isGroup(newThreadId)).thenReturn(false);
        when(itemServiceClient.isItem(newThreadId)).thenReturn(true);
        when(itemServiceClient.canReadItem(newThreadId)).thenReturn(false);
        String url = ENDPOINT + "/" + newThreadId;
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddParent_notFound() throws Exception {
        UUID newThreadId = UUID.randomUUID();
        when(itemServiceClient.isGroup(newThreadId)).thenReturn(false);
        when(itemServiceClient.isItem(newThreadId)).thenReturn(false);
        String url = ENDPOINT + "/" + newThreadId;
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testAddParent_unauthorized() throws Exception {
        UUID newThreadId = UUID.randomUUID();
        when(itemServiceClient.isGroup(newThreadId)).thenReturn(false);
        when(itemServiceClient.isItem(newThreadId)).thenReturn(false);
        String url = ENDPOINT + "/" + newThreadId;
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddChild_ok() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId() + "/child";
        String requestBody = "test_text";
        ResultActions resultActions = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(requestBody);
        assertThat(dto.getParentId()).isEqualTo(comment1.getId());
        assertThat(dto.getUserId().toString()).isEqualTo(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddChild_badRequest_wrongPermission() throws Exception {
        when(itemServiceClient.canReadGroup(any())).thenReturn(false);
        String url = ENDPOINT + "/" + comment1.getId() + "/child";
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddChild_badRequest_parentHasParent() throws Exception {
        String url = ENDPOINT + "/" + comment2.getId() + "/child";
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAddChild_notFound() throws Exception {
        String url = ENDPOINT + "/" + UUID.randomUUID() + "/child";
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testAddChild_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId() + "/child";
        String requestBody = "test_text";
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_ok() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId();
        String requestBody = "new_test_text";
        ResultActions resultActions = mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(requestBody);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_badRequest_notOwnComment() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        String requestBody = "new_test_text";
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_badRequest_wrongPermission() throws Exception {
        when(itemServiceClient.canReadGroup(any())).thenReturn(false);
        String url = ENDPOINT + "/" + comment3.getId();
        String requestBody = "new_test_text";
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_notFound() throws Exception {
        String url = ENDPOINT + "/" + UUID.randomUUID();
        String requestBody = "new_test_text";
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testEdit_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        String requestBody = "new_test_text";
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testDelete_ok() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId();
        mvc.perform(delete(url))
                .andExpect(status().isOk());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testDelete_badRequest_notOwnComment() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        mvc.perform(delete(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testDelete_badRequest_wrongPermission() throws Exception {
        when(itemServiceClient.canReadGroup(any())).thenReturn(false);
        String url = ENDPOINT + "/" + comment3.getId();
        mvc.perform(delete(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testDelete_notFound() throws Exception {
        String url = ENDPOINT + "/" + UUID.randomUUID();
        mvc.perform(delete(url))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testDelete_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        mvc.perform(delete(url))
                .andExpect(status().isUnauthorized());
    }

}
