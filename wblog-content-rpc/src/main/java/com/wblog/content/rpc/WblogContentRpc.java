package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;

public interface WblogContentRpc {
    public byte[] addWblogContent(byte[] param) throws InvalidProtocolBufferException;
    public byte[] findWblogContentByPage(byte[] param) throws InvalidProtocolBufferException;
    public byte[] deleteWblog(byte[] param) throws InvalidProtocolBufferException;
}
