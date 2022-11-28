package com.persoff68.fatodo.model;

import com.persoff68.fatodo.config.constant.AppConstants;
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
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ftd_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"thread", "reactions"})
@ToString(exclude = {"thread", "reactions"})
public class Comment extends AbstractAuditingModel implements Serializable {
    @Serial
    private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

    @ManyToOne
    private CommentThread thread;

    @NotNull
    private UUID userId;

    private String text;

    private boolean isDeleted = false;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "comment", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Reaction> reactions = new HashSet<>();

    public static Comment of(UUID userId, CommentThread thread, String text) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setThread(thread);
        comment.setText(text);
        return comment;
    }

}
