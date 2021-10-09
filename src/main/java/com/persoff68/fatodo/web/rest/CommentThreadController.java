package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.service.CommentThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(CommentThreadController.ENDPOINT)
@RequiredArgsConstructor
@Transactional
public class CommentThreadController {
    static final String ENDPOINT = "/api/threads";

    private final CommentThreadService commentThreadService;

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteAllByTargetIds(@RequestBody List<UUID> targetIdList) {
        commentThreadService.deleteByTargetIds(targetIdList);
        return ResponseEntity.ok().build();
    }

}
