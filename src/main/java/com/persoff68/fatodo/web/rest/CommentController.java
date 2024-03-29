package com.persoff68.fatodo.web.rest;

import com.persoff68.fatodo.mapper.CommentMapper;
import com.persoff68.fatodo.model.Comment;
import com.persoff68.fatodo.model.PageableList;
import com.persoff68.fatodo.model.dto.CommentDTO;
import com.persoff68.fatodo.model.vm.CommentVM;
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

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(CommentController.ENDPOINT)
@RequiredArgsConstructor
@Transactional
public class CommentController {
    static final String ENDPOINT = "/api/comment";

    private static final int DEFAULT_SIZE = 30;

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping("/{targetId}")
    public ResponseEntity<PageableList<CommentDTO>> getAllPageable(
            @PathVariable UUID targetId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer size
    ) {
        offset = Optional.ofNullable(offset).orElse(0);
        size = Optional.ofNullable(size).orElse(DEFAULT_SIZE);
        Pageable pageRequest = OffsetPageRequest.of(offset, size);
        PageableList<Comment> commentList = commentService.getAllByTargetIdPageable(targetId, pageRequest);
        PageableList<CommentDTO> dtoList = commentList.convert(commentMapper::pojoToDTO);
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{targetId}")
    public ResponseEntity<CommentDTO> add(@PathVariable UUID targetId,
                                          @Valid @RequestBody CommentVM commentVM) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Comment comment = commentService.add(userId, targetId, commentVM.getText());
        CommentDTO dto = commentMapper.pojoToDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> edit(@PathVariable UUID commentId,
                                           @Valid @RequestBody CommentVM commentVM) {
        UUID userId = SecurityUtils.getCurrentId().orElseThrow(UnauthorizedException::new);
        Comment comment = commentService.edit(userId, commentId, commentVM.getText());
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
