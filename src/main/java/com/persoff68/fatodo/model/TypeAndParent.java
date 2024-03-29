package com.persoff68.fatodo.model;

import com.persoff68.fatodo.model.constant.CommentThreadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeAndParent {

    private CommentThreadType type;

    private UUID parentId;

}

