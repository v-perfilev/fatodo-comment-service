package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.model.vm.CommentVM;
import lombok.Builder;

public class TestCommentVM extends CommentVM {
    private static final String DEFAULT_VALUE = "test_value";

    @Builder
    public TestCommentVM(String text) {
        super();
        super.setText(text);
    }

    public static TestCommentVMBuilder defaultBuilder() {
        return TestCommentVM.builder().text(DEFAULT_VALUE);
    }

    public CommentVM toParent() {
        CommentVM vm = new CommentVM();
        vm.setText(getText());
        return vm;
    }

}
