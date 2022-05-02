package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.builder.TestCommentDTO;
import com.persoff68.fatodo.builder.TestReactionsDTO;
import com.persoff68.fatodo.builder.TestWsEventDTO;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureStubRunner(ids = {"com.persoff68.fatodo:wsservice:+:stubs"},
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class WsServiceCT {

    @Autowired
    WsServiceClient wsServiceClient;

    @Test
    void testSendCommentNewEvent() {
        CommentDTO commentDTO = TestCommentDTO.defaultBuilder().id(UUID.randomUUID()).build().toParent();
        WsEventDTO<CommentDTO> dto = TestWsEventDTO.<CommentDTO>defaultBuilder().content(commentDTO).build().toParent();
        assertDoesNotThrow(() -> wsServiceClient.sendCommentNewEvent(dto));
    }

    @Test
    void testSendCommentUpdateEvent() {
        CommentDTO commentDTO = TestCommentDTO.defaultBuilder().id(UUID.randomUUID()).build().toParent();
        WsEventDTO<CommentDTO> dto = TestWsEventDTO.<CommentDTO>defaultBuilder().content(commentDTO).build().toParent();
        assertDoesNotThrow(() -> wsServiceClient.sendCommentUpdateEvent(dto));
    }

    @Test
    void testSendReactionsEvent() {
        ReactionsDTO reactionsDTO = TestReactionsDTO.defaultBuilder().build().toParent();
        WsEventDTO<ReactionsDTO> dto = TestWsEventDTO.<ReactionsDTO>defaultBuilder()
                .content(reactionsDTO).build().toParent();
        assertDoesNotThrow(() -> wsServiceClient.sendReactionsEvent(dto));
    }

}
