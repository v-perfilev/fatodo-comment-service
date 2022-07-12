package com.persoff68.fatodo.web.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.builder.TestComment;
import com.persoff68.fatodo.builder.TestCommentThread;
import com.persoff68.fatodo.builder.TestCommentVM;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.PageableList;
import com.persoff68.fatodo.model.TypeAndParent;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import com.persoff68.fatodo.web.rest.vm.CommentVM;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
class CommentControllerIT {
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
    CommentThread thread2;
    Comment comment1;
    Comment comment2;
    Comment comment3;
    Comment comment4;

    @BeforeEach
    void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        thread1 = createCommentThread();
        thread2 = createCommentThread();
        comment1 = createComment(thread1, null, USER_ID_1);
        comment2 = createComment(thread1, comment1, USER_ID_1);
        comment3 = createComment(thread1, null, USER_ID_2);
        comment4 = createComment(thread2, null, USER_ID_1);

        doNothing().when(wsServiceClient).sendCommentNewEvent(any());
        doNothing().when(wsServiceClient).sendCommentUpdateEvent(any());
        doNothing().when(wsServiceClient).sendReactionsEvent(any());

        TypeAndParent typeAndParent = new TypeAndParent(CommentThreadType.ITEM, UUID.randomUUID());
        when(itemServiceClient.getTypeAndParent(any())).thenReturn(typeAndParent);
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(true);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllPageable_ok_withoutParams() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId();
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(PageableList.class, CommentDTO.class);
        PageableList<CommentDTO> resultPageableList = objectMapper.readValue(resultString, javaType);
        List<CommentDTO> dtoList = resultPageableList.getData();
        long count = resultPageableList.getCount();
        assertThat(dtoList).hasSize(3);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testGetAllPageable_ok_withParams() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId() + "?offset=1&size=10";
        ResultActions resultActions = mvc.perform(get(url))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(PageableList.class, CommentDTO.class);
        PageableList<CommentDTO> resultPageableList = objectMapper.readValue(resultString, javaType);
        List<CommentDTO> dtoList = resultPageableList.getData();
        long count = resultPageableList.getCount();
        assertThat(dtoList).hasSize(2);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @WithAnonymousUser
    void testGetAllPageable_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId();
        mvc.perform(get(url))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_ok_existingThread() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId();
        CommentVM vm = TestCommentVM.defaultBuilder().build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        ResultActions resultActions = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(vm.getText());
        assertThat(dto.getUserId()).hasToString(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_ok_newThread() throws Exception {
        UUID newTargetId = UUID.randomUUID();
        when(itemServiceClient.hasItemsPermission(any(), any())).thenReturn(true);
        String url = ENDPOINT + "/" + newTargetId;
        CommentVM vm = TestCommentVM.defaultBuilder().build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        ResultActions resultActions = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(vm.getText());
        assertThat(dto.getUserId()).hasToString(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_ok_withReference() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId();
        CommentVM vm = TestCommentVM.defaultBuilder().referenceId(comment3.getId()).build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        ResultActions resultActions = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(vm.getText());
        assertThat(dto.getReference().getId()).isEqualTo(vm.getReferenceId());
        assertThat(dto.getUserId()).hasToString(USER_ID_1);
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_withReference_badRequest_invalidModel() throws Exception {
        String url = ENDPOINT + "/" + thread1.getTargetId();
        CommentVM vm = TestCommentVM.defaultBuilder().referenceId(comment4.getId()).build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_forbidden_wrongPermission() throws Exception {
        UUID newTargetId = UUID.randomUUID();
        when(itemServiceClient.hasItemsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/" + newTargetId;
        CommentVM vm = TestCommentVM.defaultBuilder().build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testAdd_notFound() throws Exception {
        UUID newTargetId = UUID.randomUUID();
        doThrow(new ModelNotFoundException()).when(itemServiceClient).getTypeAndParent(newTargetId);
        String url = ENDPOINT + "/" + newTargetId;
        CommentVM vm = TestCommentVM.defaultBuilder().build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testAdd_unauthorized() throws Exception {
        UUID newTargetId = UUID.randomUUID();
        String url = ENDPOINT + "/" + newTargetId;
        CommentVM vm = TestCommentVM.defaultBuilder().build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_ok() throws Exception {
        String url = ENDPOINT + "/" + comment1.getId();
        CommentVM vm = TestCommentVM.defaultBuilder().text("new").build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        ResultActions resultActions = mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
        String resultString = resultActions.andReturn().getResponse().getContentAsString();
        CommentDTO dto = objectMapper.readValue(resultString, CommentDTO.class);
        assertThat(dto.getText()).isEqualTo(vm.getText());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_forbidden_notOwnComment() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        CommentVM vm = TestCommentVM.defaultBuilder().text("new").build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_forbidden_wrongPermission() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/" + comment3.getId();
        CommentVM vm = TestCommentVM.defaultBuilder().text("new").build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testEdit_notFound() throws Exception {
        String url = ENDPOINT + "/" + UUID.randomUUID();
        CommentVM vm = TestCommentVM.defaultBuilder().text("new").build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
        mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void testEdit_unauthorized() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        CommentVM vm = TestCommentVM.defaultBuilder().text("new").build().toParent();
        String requestBody = objectMapper.writeValueAsString(vm);
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
    void testDelete_forbidden_notOwnComment() throws Exception {
        String url = ENDPOINT + "/" + comment3.getId();
        mvc.perform(delete(url))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithCustomSecurityContext(id = USER_ID_1)
    void testDelete_forbidden_wrongPermission() throws Exception {
        when(itemServiceClient.hasGroupsPermission(any(), any())).thenReturn(false);
        String url = ENDPOINT + "/" + comment3.getId();
        mvc.perform(delete(url))
                .andExpect(status().isForbidden());
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

    private CommentThread createCommentThread() {
        CommentThread thread = TestCommentThread.defaultBuilder().type(CommentThreadType.GROUP).build().toParent();
        return threadRepository.saveAndFlush(thread);
    }

    private Comment createComment(CommentThread thread, Comment reference, String userId) {
        Comment comment = TestComment.defaultBuilder().thread(thread).reference(reference)
                .userId(UUID.fromString(userId)).build().toParent();
        return commentRepository.saveAndFlush(comment);
    }

}
