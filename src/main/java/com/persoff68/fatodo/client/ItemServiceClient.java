package com.persoff68.fatodo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "item-service", primary = false)
public interface ItemServiceClient {

    @PostMapping(value = "/api/permissions/group/{groupId}")
    boolean canReadGroup(@PathVariable UUID groupId);

    @PostMapping(value = "/api/permissions/item/{itemId}")
    boolean canReadItem(@PathVariable UUID itemId);

    @GetMapping(value = "/api/check/is-group/{groupId}")
    boolean isGroup(@PathVariable UUID groupId);

    @GetMapping(value = "/api/check/is-item/{itemId}")
    boolean isItem(@PathVariable UUID itemId);

    @GetMapping(value = "/api/groups/{groupId}/user-ids")
    List<UUID> getGroupUserIdsById(@PathVariable UUID groupId);

    @GetMapping(value = "/api/items/{itemId}/user-ids")
    List<UUID> getItemUserIdsById(@PathVariable UUID itemId);
}
