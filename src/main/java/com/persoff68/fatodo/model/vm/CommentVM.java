package com.persoff68.fatodo.model.vm;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentVM {

    @NotEmpty
    private String text;

}
