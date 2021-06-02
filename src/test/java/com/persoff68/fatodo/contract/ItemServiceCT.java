package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.client.ItemServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureStubRunner(ids = {"com.persoff68.fatodo:itemservice:+:stubs"},
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
public class ItemServiceCT {

    @Autowired
    ItemServiceClient itemServiceClient;

    @Test
    void testCanReadGroup() {
        boolean canRead = itemServiceClient.canReadGroup(UUID.randomUUID());
        assertThat(canRead).isTrue();
    }

    @Test
    void testCanReadItem() {
        boolean canRead = itemServiceClient.canReadItem(UUID.randomUUID());
        assertThat(canRead).isTrue();
    }

    @Test
    void testIsGroup() {
        boolean isGroup = itemServiceClient.isGroup(UUID.randomUUID());
        assertThat(isGroup).isTrue();
    }

    @Test
    void testIsItem() {
        boolean isItem = itemServiceClient.isItem(UUID.randomUUID());
        assertThat(isItem).isTrue();
    }

    @Test
    void testGetGroupUserIds() {
        List<UUID> userIdList = itemServiceClient.getGroupUserIdsById(UUID.randomUUID());
        assertThat(userIdList).isNotEmpty();
    }

    @Test
    void testGetItemUserIds() {
        List<UUID> userIdList = itemServiceClient.getItemUserIdsById(UUID.randomUUID());
        assertThat(userIdList).isNotEmpty();
    }

}
