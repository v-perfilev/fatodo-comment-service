package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.mapper.CommentMapper;
import com.persoff68.fatodo.repository.OffsetPageRequest;
import com.persoff68.fatodo.security.exception.UnauthorizedException;
import com.persoff68.fatodo.security.util.SecurityUtils;
import com.persoff68.fatodo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(CommentController.ENDPOINT)
@RequiredArgsConstructor
@Transactional
public class CommentController {
    static final String ENDPOINT = "/api/comments";

    private static final int DEFAULT_SIZE = 30;

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping("/{targetId}")
    public ResponseEntity<List<CommentDTO>> getAllParentsPageable(
            @PathVariable UUID targetId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer size
    ) {
        offset = Optional.ofNullable(offset).orElse(0);
        size = Optional.ofNullable(size).orElse(DEFAULT_SIZE);
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Pageable pageRequest = OffsetPageRequest.of(offset, size);
        List<Comment> commentList = commentService.getParentsByTargetIdPageable(userId, targetId, pageRequest);
        List<CommentDTO> dtoList = commentList.stream()
                .map(commentMapper::pojoToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CommentDTO>> getAllChildrenPageable(
            @PathVariable UUID parentId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer size
    ) {
        offset = Optional.ofNullable(offset).orElse(0);
        size = Optional.ofNullable(size).orElse(DEFAULT_SIZE);
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Pageable pageRequest = OffsetPageRequest.of(offset, size);
        List<Comment> commentList = commentService.getChildrenByParentIdPageable(userId, parentId, pageRequest);
        List<CommentDTO> dtoList = commentList.stream()
                .map(commentMapper::pojoToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{targetId}")
    public ResponseEntity<CommentDTO> addParent(@PathVariable UUID targetId,
                                                @RequestBody String text) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Comment comment = commentService.addParent(userId, targetId, text);
        CommentDTO dto = commentMapper.pojoToDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{parentId}/child")
    public ResponseEntity<CommentDTO> addChild(@PathVariable UUID parentId,
                                               @RequestBody String text) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Comment comment = commentService.addChild(userId, parentId, text);
        CommentDTO dto = commentMapper.pojoToDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> edit(@PathVariable UUID commentId,
                                           @RequestBody String text) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Comment comment = commentService.edit(userId, commentId, text);
        CommentDTO dto = commentMapper.pojoToDTO(comment);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDTO> delete(@PathVariable UUID commentId) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        commentService.delete(userId, commentId);
        return ResponseEntity.ok().build();
    }

}
