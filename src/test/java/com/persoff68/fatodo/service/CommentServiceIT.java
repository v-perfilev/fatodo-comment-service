package com.persoff68.fatodo.service;

import com.persoff68.fatodo.FatodoCommentServiceApplication;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThread;
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
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FatodoCommentServiceApplication.class)
@AutoConfigureMockMvc
public class CommentServiceIT {
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

    CommentThread thread;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    public void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        when(itemServiceClient.isGroup(any())).thenReturn(false);
        when(itemServiceClient.isItem(any())).thenReturn(true);
        when(itemServiceClient.canReadItem(any())).thenReturn(true);

        doNothing().when(wsServiceClient).sendCommentNewEvent(any());

        this.comment1 = commentService.add(USER_1_ID, TARGET_ID, "test", null);
        this.comment2 = commentService.add(USER_1_ID, TARGET_ID, "test", this.comment1.getId());
        this.comment3 = commentService.add(USER_2_ID, TARGET_ID, "test", this.comment1.getId());
        this.thread = this.comment1.getThread();
    }

    @Test
    public void testAllCommentsAreInDb() {
        List<Comment> commentList = commentRepository.findAll();
        Comment comment = commentRepository.findById(this.comment1.getId()).orElse(null);
        assertThat(commentList.size()).isEqualTo(3);
        assertThat(comment).isNotNull();
    }

    @Test
    public void testGetAllByTargetId() {
        Pair<List<Comment>, Long> pair = commentService
                .getAllByTargetIdPageable(this.thread.getTargetId(), pageable);
        List<Comment> data = pair.getFirst();
        long count = pair.getSecond();
        assertThat(data.size()).isEqualTo(3);
        assertThat(count).isEqualTo(3L);
    }

}
