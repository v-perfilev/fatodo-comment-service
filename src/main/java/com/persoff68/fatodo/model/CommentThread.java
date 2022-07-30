package com.persoff68.fatodo.model;

import com.persoff68.fatodo.config.constant.AppConstants;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
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
public class CommentThread extends AbstractModel implements Serializable {
    @Serial
    private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

    @NotNull
    private UUID parentId;

    @NotNull
    private UUID targetId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommentThreadType type;

    @OneToMany(cascade = {CascadeType.REMOVE}, mappedBy = "thread")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "thread")
    private List<ReadStatus> readStatuses;

    public static CommentThread of(UUID parentId, UUID targetId, CommentThreadType type) {
        CommentThread thread = new CommentThread();
        thread.setParentId(parentId);
        thread.setTargetId(targetId);
        thread.setType(type);
        return thread;
    }

}
