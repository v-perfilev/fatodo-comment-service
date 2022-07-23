package com.persoff68.fatodo.client;

import com.persoff68.fatodo.client.configuration.FeignAuthConfiguration;
import com.persoff68.fatodo.model.TypeAndParent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "item-service", primary = false,
        configuration = {FeignAuthConfiguration.class},
        qualifiers = {"feignItemServiceClient"})
public interface ItemServiceClient {

    @GetMapping(value = "/api/permission/group/{permission}/check")
    boolean hasGroupsPermission(@PathVariable String permission, @RequestParam("ids") List<UUID> groupIdList);

    @GetMapping(value = "/api/permission/group/{permission}/ids")
    List<UUID> getAllowedGroupIds(@PathVariable String permission, @RequestParam("ids") List<UUID> groupIdList);

    @GetMapping(value = "/api/permission/item/{permission}/check")
    boolean hasItemsPermission(@PathVariable String permission, @RequestParam("ids") List<UUID> itemIdList);

    @GetMapping(value = "/api/permission/item/{permission}/ids")
    List<UUID> getAllowedItemIds(@PathVariable String permission, @RequestParam("ids") List<UUID> itemIdList);

    @GetMapping(value = "/api/check/type-and-parent/{id}")
    TypeAndParent getTypeAndParent(@PathVariable UUID id);

    @GetMapping(value = "/api/member/{groupId}")
    List<UUID> getUserIdsByGroupId(@PathVariable UUID groupId);

    @GetMapping(value = "/api/member/{itemId}/item")
    List<UUID> getUserIdsByItemId(@PathVariable UUID itemId);
}
