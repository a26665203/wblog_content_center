package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wblog.content.service.WblogContentService;

import com.wblog.pojo.WblogContentPojo;
import com.wblog.proto.WblogContentProto;
import com.weblog.content.common.WblogContentResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("WblogContentRpc")
public class WblogContentRpcImpl implements WblogContentRpc {
    @Autowired
    WblogContentService wblogContentService;

    @Override
    public byte[] addWblogContent(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.AddWblogContentReq req = WblogContentProto.AddWblogContentReq.parseFrom(param);
        WblogContentPojo wblogContentPojo = new WblogContentPojo();
        BeanUtils.copyProperties(req.getPojo(),wblogContentPojo);
        WblogContentResult<Boolean> result = wblogContentService.addWblogContent(wblogContentPojo);
        WblogContentProto.AddWblogContentRes.Builder builder = WblogContentProto.AddWblogContentRes.newBuilder();
        builder.setCode(result.getCode());
        builder.setDesc(result.getDesc());
        builder.setResult(result.getResult());
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogContentByPage(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.FindWblogContentByPageReq req = WblogContentProto.FindWblogContentByPageReq.parseFrom(param);
        WblogContentResult<List<WblogContentPojo>> wblogContentByPage = wblogContentService.findWblogContentByPage(req.getPage(), req.getCreator(), req.getSize());
        WblogContentProto.FindWblogContentByPageRes.Builder builder = WblogContentProto.FindWblogContentByPageRes.newBuilder();
        List<WblogContentPojo> mid = wblogContentByPage.getResult();
        builder.setCode(wblogContentByPage.getCode());
        builder.setDesc(wblogContentByPage.getDesc());
        for(int i = 0 ;i<mid.size();i++){
            WblogContentProto.WblogContentPojo wblogContentPojo = WblogContentProto.WblogContentPojo.newBuilder().build();
            BeanUtils.copyProperties(mid.get(i),wblogContentPojo);
            builder.setResult(i,wblogContentPojo);
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] deleteWblog(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.DeleteWblogContentReq req = WblogContentProto.DeleteWblogContentReq.parseFrom(param);
        WblogContentResult<Boolean> result = wblogContentService.deleteWblog(req.getId(), req.getNickName());
        WblogContentProto.DeleteWblogContentRes.Builder builder = WblogContentProto.DeleteWblogContentRes.newBuilder();
        builder.setCode(result.getCode());
        builder.setDesc(result.getDesc());
        builder.setResult(result.getResult());
        return builder.build().toByteArray();
    }
}
