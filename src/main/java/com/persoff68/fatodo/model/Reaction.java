package com.persoff68.fatodo.model;

import com.persoff68.fatodo.config.constant.AppConstants;
import com.persoff68.fatodo.model.constant.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ftd_comment_reaction")
@Data
@NoArgsConstructor
@IdClass(Reaction.ReactionId.class)
@ToString(exclude = {"comment"})
public class Reaction implements Serializable {
    @Serial
    private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

    @Id
    @ManyToOne
    private Comment comment;

    @Id
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReactionType type;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    public static Reaction of(Comment comment, UUID userId, ReactionType type) {
        Reaction reaction = new Reaction();
        reaction.comment = comment;
        reaction.userId = userId;
        reaction.type = type;
        return reaction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionId implements Serializable {
        @Serial
        private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

        private Comment comment;
        private UUID userId;
    }

}
