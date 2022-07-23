package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.dto.CommentInfoDTO;
import com.persoff68.fatodo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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

}
