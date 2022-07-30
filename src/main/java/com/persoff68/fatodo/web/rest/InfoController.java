package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.CommentThreadInfo;
import com.persoff68.fatodo.model.dto.CommentInfoDTO;
import com.persoff68.fatodo.model.dto.ThreadInfoDTO;
import com.persoff68.fatodo.security.exception.UnauthorizedException;
import com.persoff68.fatodo.security.util.SecurityUtils;
import com.persoff68.fatodo.service.CommentService;
import com.persoff68.fatodo.service.CommentThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(InfoController.ENDPOINT)
@RequiredArgsConstructor
@Transactional
public class InfoController {
    static final String ENDPOINT = "/api/info";

    private final CommentThreadService commentThreadService;
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping(value = "/comment")
    public ResponseEntity<List<CommentInfoDTO>> getAllCommentInfoByIds(@RequestParam("ids") List<UUID> commentIdList) {
        List<Comment> commentList = commentService.getAllAllowedByIds(commentIdList);
        List<CommentInfoDTO> dtoList = commentList.stream()
                .map(commentMapper::pojoToInfoDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping(value = "/thread")
    public ResponseEntity<List<ThreadInfoDTO>> getInfoByTargetIds(@RequestParam("ids") List<UUID> targetIdList) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        List<CommentThreadInfo> threadInfoList = commentThreadService.getInfoByTargetIds(userId, targetIdList);
        List<ThreadInfoDTO> threadInfoDTOList = threadInfoList.stream()
                .map(commentMapper::threadInfoToDTO)
                .toList();
        return ResponseEntity.ok(threadInfoDTOList);
    }

    @PutMapping("/thread/{targetId}/refresh")
    public ResponseEntity<Void> refresh(@PathVariable UUID targetId) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        commentThreadService.refreshReadStatus(userId, targetId);
        return ResponseEntity.ok().build();
    }

}
