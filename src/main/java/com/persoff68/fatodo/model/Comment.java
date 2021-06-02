package com.persoff68.fatodo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ftd_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"thread", "children"})
public class Comment extends AbstractAuditingModel {

    @ManyToOne
    private CommentThread thread;

    @NotNull
    private UUID userId;

    private String text;

    private boolean isDeleted = false;

    @OneToMany(cascade = {CascadeType.REMOVE}, mappedBy = "comment", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Reaction> reactions = new ArrayList<>();

    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private List<Comment> children = new ArrayList<>();

    public static Comment of(UUID userId, CommentThread thread, String text) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setThread(thread);
        comment.setText(text);
        return comment;
    }

    public static Comment of(UUID userId, Comment parent, String text) {
        CommentThread thread = parent.getThread();
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setThread(thread);
        comment.setText(text);
        comment.setParent(parent);
        return comment;
    }

}
