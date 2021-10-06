package com.persoff68.fatodo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "item-service", primary = false)
public interface ItemServiceClient {

    @GetMapping(value = "/api/permissions/group/{groupId}")
    boolean canReadGroup(@PathVariable UUID groupId);

    @GetMapping(value = "/api/permissions/item/{itemId}")
    boolean canReadItem(@PathVariable UUID itemId);

    @GetMapping(value = "/api/check/is-group/{groupId}")
    boolean isGroup(@PathVariable UUID groupId);

    @GetMapping(value = "/api/check/is-item/{itemId}")
    boolean isItem(@PathVariable UUID itemId);

    @GetMapping(value = "/api/members/group/{groupId}/user-ids")
    List<UUID> getUserIdsByGroupId(@PathVariable UUID groupId);

    @GetMapping(value = "/api/members/item/{itemId}/user-ids")
    List<UUID> getUserIdsByItemId(@PathVariable UUID itemId);
}
