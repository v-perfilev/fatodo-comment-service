package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.builder.TestWsEventWithUsersDTO;
import com.persoff68.fatodo.client.WsServiceClient;
import com.persoff68.fatodo.model.dto.WsEventWithUsersDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureStubRunner(ids = {"com.persoff68.fatodo:wsservice:+:stubs"},
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class WsServiceCT {

    @Autowired
    WsServiceClient wsServiceClient;

    @Test
    void testSendEvent() {
        WsEventWithUsersDTO dto = TestWsEventWithUsersDTO.defaultBuilder().build();
        assertDoesNotThrow(() -> wsServiceClient.sendEvent(dto));
    }

}
