package com.persoff68.fatodo.contract;

import com.persoff68.fatodo.annotation.WithCustomSecurityContext;
import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.model.TypeAndParent;
import com.persoff68.fatodo.model.constant.CommentThreadType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureStubRunner(ids = {"com.persoff68.fatodo:itemservice:+:stubs"},
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class ItemServiceCT {

    @Autowired
    ItemServiceClient itemServiceClient;

    @Test
    @WithCustomSecurityContext
    void testHasGroupsPermission() {
        List<UUID> targetIdList = Collections.singletonList(UUID.randomUUID());
        boolean canRead = itemServiceClient.hasGroupsPermission("READ", targetIdList);
        assertThat(canRead).isTrue();
    }

    @Test
    @WithCustomSecurityContext
    void testGetAllowedGroupIds() {
        List<UUID> targetIdList = Collections.singletonList(UUID.randomUUID());
        List<UUID> groupIdList = itemServiceClient.getAllowedGroupIds("READ", targetIdList);
        assertThat(groupIdList).isNotEmpty();
    }

    @Test
    @WithCustomSecurityContext
    void testHasItemsPermission() {
        List<UUID> targetIdList = Collections.singletonList(UUID.randomUUID());
        boolean canRead = itemServiceClient.hasItemsPermission("READ", targetIdList);
        assertThat(canRead).isTrue();
    }

    @Test
    @WithCustomSecurityContext
    void testGetAllowedItemIds() {
        List<UUID> targetIdList = Collections.singletonList(UUID.randomUUID());
        List<UUID> itemIdList = itemServiceClient.getAllowedItemIds("READ", targetIdList);
        assertThat(itemIdList).isNotEmpty();
    }

    @Test
    @WithCustomSecurityContext
    void testGetTypeAndParent() {
        TypeAndParent typeAndParent = itemServiceClient.getTypeAndParent(UUID.randomUUID());
        assertThat(typeAndParent.getType()).isEqualTo(CommentThreadType.ITEM);
        assertThat(typeAndParent.getParentId()).isNotNull();
    }

    @Test
    @WithCustomSecurityContext
    void testGetGroupUserIds() {
        List<UUID> userIdList = itemServiceClient.getUserIdsByGroupId(UUID.randomUUID());
        assertThat(userIdList).isNotEmpty();
    }

    @Test
    @WithCustomSecurityContext
    void testGetItemUserIds() {
        List<UUID> userIdList = itemServiceClient.getUserIdsByItemId(UUID.randomUUID());
        assertThat(userIdList).isNotEmpty();
    }

}
