package com.persoff68.fatodo.client;

import com.persoff68.fatodo.model.TypeAndParent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "item-service", primary = false, qualifiers = {"feignItemServiceClient"})
public interface ItemServiceClient {

    @GetMapping(value = "/api/permissions/group/read/{groupId}")
    boolean canReadGroup(@PathVariable UUID groupId);

    @GetMapping(value = "/api/permissions/group/admin/{groupId}")
    boolean canAdminGroup(@PathVariable UUID groupId);

    @PostMapping(value = "/api/permissions/groups/admin")
    boolean canAdminGroups(@RequestBody List<UUID> groupIdList);

    @GetMapping(value = "/api/permissions/item/read/{itemId}")
    boolean canReadItem(@PathVariable UUID itemId);

    @GetMapping(value = "/api/permissions/item/admin/{itemId}")
    boolean canAdminItem(@PathVariable UUID itemId);

    @PostMapping(value = "/api/permissions/items/admin")
    boolean canAdminItems(@RequestBody List<UUID> itemIdList);

    @GetMapping(value = "/api/check/type-and-parent/{id}")
    TypeAndParent getTypeAndParent(@PathVariable UUID id);

    @GetMapping(value = "/api/members/group/{groupId}/ids")
    List<UUID> getUserIdsByGroupId(@PathVariable UUID groupId);

    @GetMapping(value = "/api/members/item/{itemId}/ids")
    List<UUID> getUserIdsByItemId(@PathVariable UUID itemId);
}
