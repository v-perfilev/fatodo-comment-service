package com.persoff68.fatodo.model;

import com.persoff68.fatodo.model.constant.CommentThreadType;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface CommentThreadInfo {

    byte[] getIdBytes();

    byte[] getParentIdBytes();

    byte[] getTargetIdBytes();

    CommentThreadType getType();

    int getCount();

    int getUnread();

    default UUID getId() {
        byte[] bytes = getIdBytes();
        return bytesToUUID(bytes);
    }

    default UUID getParentId() {
        byte[] bytes = getParentIdBytes();
        return bytesToUUID(bytes);
    }

    default UUID getTargetId() {
        byte[] bytes = getTargetIdBytes();
        return bytesToUUID(bytes);
    }

    default UUID bytesToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

}