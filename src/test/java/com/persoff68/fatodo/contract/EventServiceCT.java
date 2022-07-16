package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.client.EventServiceClient;
import com.persoff68.fatodo.model.dto.CreateCommentEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureStubRunner(ids = {"com.persoff68.fatodo:eventservice:+:stubs"},
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class EventServiceCT {

    @Autowired
    EventServiceClient eventServiceClient;

    @Test
    void testAddCommentEvent() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        List<UUID> recipientIdList = List.of(userId1, userId2);
        CreateCommentEventDTO dto = CreateCommentEventDTO.commentAdd(recipientIdList, userId1, parentId, targetId,
                commentId);
        assertDoesNotThrow(() -> eventServiceClient.addCommentEvent(dto));
    }

}
