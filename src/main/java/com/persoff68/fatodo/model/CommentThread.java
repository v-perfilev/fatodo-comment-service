package com.persoff68.fatodo.model;

import com.persoff68.fatodo.model.constant.CommentThreadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ftd_comment_thread")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"comments"})
public class CommentThread extends AbstractModel {

    @NotNull
    private UUID targetId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommentThreadType type;

    @OneToMany(mappedBy = "thread", cascade = {CascadeType.REFRESH, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    public static CommentThread of(UUID targetId, CommentThreadType type) {
        CommentThread thread = new CommentThread();
        thread.setTargetId(targetId);
        thread.setType(type);
        return thread;
    }

}
