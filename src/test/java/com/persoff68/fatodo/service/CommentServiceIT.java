package com.persoff68.fatodo.service;

import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
import com.persoff68.fatodo.model.PageableList;
import com.persoff68.fatodo.model.TypeAndParent;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import com.persoff68.fatodo.repository.CommentRepository;
import com.persoff68.fatodo.repository.CommentThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
class CommentServiceIT {
    private static final Pageable pageable = PageRequest.of(0, 100);
    private static final UUID USER_1_ID = UUID.randomUUID();
    private static final UUID USER_2_ID = UUID.randomUUID();
    private static final UUID TARGET_ID = UUID.randomUUID();

    @Autowired
    MockMvc mvc;

    @Autowired
    CommentThreadRepository threadRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentService commentService;

    @MockBean
    ItemServiceClient itemServiceClient;
    @MockBean
    WsServiceClient wsServiceClient;
    @MockBean
    EventServiceClient eventServiceClient;

    CommentThread thread;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        TypeAndParent typeAndParent = new TypeAndParent(CommentThreadType.ITEM, UUID.randomUUID());
        when(itemServiceClient.getTypeAndParent(any())).thenReturn(typeAndParent);
        when(itemServiceClient.hasItemsPermission(any(), any())).thenReturn(true);

        this.comment1 = commentService.add(USER_1_ID, TARGET_ID, "test");
        this.comment2 = commentService.add(USER_1_ID, TARGET_ID, "test");
        this.comment3 = commentService.add(USER_2_ID, TARGET_ID, "test");
        this.thread = this.comment1.getThread();
    }

    @Test
    void testAllCommentsAreInDb() {
        List<Comment> commentList = commentRepository.findAll();
        Comment comment = commentRepository.findById(this.comment1.getId()).orElse(null);
        assertThat(commentList).hasSize(3);
        assertThat(comment).isNotNull();
    }

    @Test
    void testGetAllByTargetId() {
        PageableList<Comment> pageableList = commentService
                .getAllByTargetIdPageable(this.thread.getTargetId(), pageable);
        assertThat(pageableList.getData()).hasSize(3);
        assertThat(pageableList.getCount()).isEqualTo(3L);
    }

}
