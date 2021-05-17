package com.persoff68.fatodo.client;

import com.persoff68.fatodo.exception.ClientException;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class WsServiceClientWrapper implements WsServiceClient {

    @Qualifier("wsServiceClient")
    private final WsServiceClient wsServiceClient;

    @Override
    public void sendCommentNewEvent(WsEventDTO<CommentDTO> event) {
        try {
            wsServiceClient.sendCommentNewEvent(event);
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public void sendCommentUpdateEvent(WsEventDTO<CommentDTO> event) {
        try {
            wsServiceClient.sendCommentUpdateEvent(event);
        } catch (Exception e) {
            throw new ClientException();
        }
    }

    @Override
    public void sendReactionsEvent(WsEventDTO<ReactionsDTO> event) {
        try {
            wsServiceClient.sendReactionsEvent(event);
        } catch (Exception e) {
            throw new ClientException();
        }
    }
}
