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
    Comment parent;
    Comment child1;
    Comment child2;

    @BeforeEach
    public void setup() {
        threadRepository.deleteAll();
        commentRepository.deleteAll();

        when(itemServiceClient.isGroup(any())).thenReturn(false);
        when(itemServiceClient.isItem(any())).thenReturn(true);
        when(itemServiceClient.canReadItem(any())).thenReturn(true);

        doNothing().when(wsServiceClient).sendCommentNewEvent(any());

        this.parent = commentService.addParent(USER_1_ID, TARGET_ID, "test");
        this.child1 = commentService.addChild(USER_1_ID, this.parent.getId(), "test");
        this.child2 = commentService.addChild(USER_2_ID, this.parent.getId(), "test");
        this.thread = this.parent.getThread();
    }

    @Test
    public void testAllCommentsAreInDb() {
        List<Comment> commentList = commentRepository.findAll();
        Comment parent = commentRepository.findById(this.parent.getId()).orElse(null);
        assertThat(commentList.size()).isEqualTo(3);
        assertThat(parent).isNotNull();
        assertThat(parent.getChildren().size()).isEqualTo(2);
    }

    @Test
    public void testGetParentsByThreadId() {
        Pair<List<Comment>, Long> pair = commentService
                .getParentsByTargetIdPageable(this.thread.getId(), pageable);
        List<Comment> data = pair.getFirst();
        long count = pair.getSecond();
        assertThat(data.size()).isEqualTo(1);
        assertThat(count).isEqualTo(1L);
        assertThat(data.get(0).getChildren().size()).isEqualTo(2);
    }

    @Test
    public void testGetChildrenByParentId() {
        Pair<List<Comment>, Long> pair = commentService
                .getChildrenByParentIdPageable(this.parent.getId(), pageable);
        List<Comment> data = pair.getFirst();
        long count = pair.getSecond();
        assertThat(data.size()).isEqualTo(2);
        assertThat(count).isEqualTo(2L);
        data.forEach(comment -> {
            assertThat(comment.getParent()).isNotNull();
            assertThat(comment.getChildren()).isEmpty();
        });
    }

}
