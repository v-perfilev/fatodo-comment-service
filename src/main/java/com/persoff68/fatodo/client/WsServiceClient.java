package com.persoff68.fatodo.client;

import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.dto.ReactionsDTO;
import com.persoff68.fatodo.model.dto.WsEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ws-service", primary = false, qualifiers = {"feignWsServiceClient"})
public interface WsServiceClient {

    @PostMapping(value = "/api/comment/new")
    void sendCommentNewEvent(@RequestBody WsEventDTO<CommentDTO> event);

    @PostMapping(value = "/api/comment/update")
    void sendCommentUpdateEvent(@RequestBody WsEventDTO<CommentDTO> event);

    @PostMapping(value = "/api/comment/reactions")
    void sendReactionsEvent(@RequestBody WsEventDTO<ReactionsDTO> event);

}

