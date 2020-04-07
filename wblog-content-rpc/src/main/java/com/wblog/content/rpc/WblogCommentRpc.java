package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;

public interface WblogCommentRpc {
    public byte[] addWblogComment(byte[] param) throws InvalidProtocolBufferException;
    public byte[] findWblogCommentByPage(byte[] param) throws InvalidProtocolBufferException;
    public byte[] deleteWblogComment(byte[] param) throws InvalidProtocolBufferException;
    public byte[] findWblogCommentByBlogId(byte[] param) throws InvalidProtocolBufferException;
}
