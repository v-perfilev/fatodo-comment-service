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
public class GroupServiceClientWrapper implements GroupServiceClient {

    @Qualifier("groupServiceClient")
    private final GroupServiceClient groupServiceClient;

    @Override
    public boolean canRead(List<UUID> groupIds) {
        try {
            return groupServiceClient.canRead(groupIds);
        } catch (FeignException.NotFound e) {
            throw new ModelNotFoundException();
        } catch (Exception e) {
            throw new ClientException();
        }
    }

}
