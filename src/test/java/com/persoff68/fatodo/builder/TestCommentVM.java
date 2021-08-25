package com.persoff68.fatodo.builder;

import com.persoff68.fatodo.web.rest.vm.CommentVM;
import lombok.Builder;

import java.util.UUID;

public class TestCommentVM extends CommentVM {
    private static final String DEFAULT_VALUE = "test_value";

    @Builder
    public TestCommentVM(String text, UUID referenceId) {
        super();
        super.setText(text);
        super.setReferenceId(referenceId);
    }

    public static TestCommentVMBuilder defaultBuilder() {
        return TestCommentVM.builder().text(DEFAULT_VALUE);
    }

    public CommentVM toParent() {
        CommentVM vm = new CommentVM();
        vm.setText(getText());
        vm.setReferenceId(getReferenceId());
        return vm;
    }

}
