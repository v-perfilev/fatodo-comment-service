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

    @GetMapping(value = "/api/permissions/is-group/{id}")
    boolean isGroup(@PathVariable UUID id);

    @GetMapping(value = "/api/permissions/is-item/{id}")
    boolean isItem(@PathVariable UUID id);

    @GetMapping(value = "/api/permissions/groups/{id}/user-ids")
    List<UUID> getGroupUserIdsById(@PathVariable UUID id);

    @GetMapping(value = "/api/permissions/items/{id}/user-ids")
    List<UUID> getItemUserIdsById(@PathVariable UUID id);
}
