package com.persoff68.fatodo.client;

import com.persoff68.fatodo.exception.ClientException;
import com.persoff68.fatodo.service.exception.ModelNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class ItemServiceClientWrapper implements ItemServiceClient {

    @Qualifier("groupServiceClient")
    private final ItemServiceClient itemServiceClient;

    @Override
    public boolean canReadGroup(UUID groupId) {
        try {
            return itemServiceClient.canReadGroup(groupId);
        } catch (FeignException.NotFound e) {
            throw new ModelNotFoundException();
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public boolean canReadItem(UUID itemId) {
        try {
            return itemServiceClient.canReadItem(itemId);
        } catch (FeignException.NotFound e) {
            throw new ModelNotFoundException();
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public boolean isGroup(UUID id) {
        try {
            return itemServiceClient.isGroup(id);
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public boolean isItem(UUID id) {
        try {
            return itemServiceClient.isItem(id);
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public List<UUID> getGroupUserIdsById(UUID id) {
        try {
            return itemServiceClient.getGroupUserIdsById(id);
        } catch (FeignException.NotFound e) {
            throw new ModelNotFoundException();
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public List<UUID> getItemUserIdsById(UUID id) {
        try {
            return itemServiceClient.getItemUserIdsById(id);
        } catch (FeignException.NotFound e) {
            throw new ModelNotFoundException();
        } catch (Exception e) {
            throw new ClientException();
        }
    }
}
