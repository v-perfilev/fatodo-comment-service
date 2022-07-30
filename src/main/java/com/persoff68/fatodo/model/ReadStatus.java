package com.persoff68.fatodo.model;

import com.persoff68.fatodo.config.constant.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ftd_comment_read_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReadStatus.ReadStatusId.class)
@ToString(exclude = {"thread"})
public class ReadStatus implements Serializable {
    @Serial
    private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

    @Id
    @ManyToOne
    private CommentThread thread;

    @Id
    private UUID userId;

    private Date lastReadAt;

    public static ReadStatus of(CommentThread thread, UUID userId) {
        ReadStatus readStatus = new ReadStatus();
        readStatus.thread = thread;
        readStatus.userId = userId;
        return readStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadStatusId implements Serializable {
        @Serial
        private static final long serialVersionUID = AppConstants.SERIAL_VERSION_UID;

        private CommentThread thread;
        private UUID userId;
    }

}
