package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.security.exception.UnauthorizedException;
import com.persoff68.fatodo.security.util.SecurityUtils;
import com.persoff68.fatodo.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ReactionController.ENDPOINT)
@RequiredArgsConstructor
public class ReactionController {
    static final String ENDPOINT = "/api/reaction";

    private final ReactionService reactionService;

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> setLike(@PathVariable UUID commentId) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        reactionService.setLike(userId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<Void> setDislike(@PathVariable UUID commentId) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        reactionService.setDislike(userId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> setNone(@PathVariable UUID commentId) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        reactionService.remove(userId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
