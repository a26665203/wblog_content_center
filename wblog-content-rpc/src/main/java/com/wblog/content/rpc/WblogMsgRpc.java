package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;

public interface WblogMsgRpc {
    public byte[] addWblogMsg(byte[] param) throws InvalidProtocolBufferException;
    public byte[] findWblogContentByPage(byte[] param) throws InvalidProtocolBufferException;
    public byte[] deleteMsg(byte[] param) throws InvalidProtocolBufferException;
}
