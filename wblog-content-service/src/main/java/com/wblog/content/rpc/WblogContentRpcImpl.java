package com.wblog.content.rpc;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.wblog.content.service.WblogContentService;

import com.wblog.pojo.WblogContentPojo;
import com.wblog.proto.WblogContentProto;
import com.weblog.content.common.WblogContentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("WblogContentRpc")
public class WblogContentRpcImpl implements WblogContentRpc {
    @Autowired
    WblogContentService wblogContentService;
    Logger logger = LoggerFactory.getLogger(WblogContentRpcImpl.class);
    @Override
    public byte[] addWblogContent(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.AddWblogContentReq req = WblogContentProto.AddWblogContentReq.parseFrom(param);
        WblogContentPojo wblogContentPojo = new WblogContentPojo();
        BeanUtils.copyProperties(req.getPojo(),wblogContentPojo);
        WblogContentResult<Boolean> result = wblogContentService.addWblogContent(wblogContentPojo);
        WblogContentProto.AddWblogContentRes.Builder builder = WblogContentProto.AddWblogContentRes.newBuilder();
        builder.setCode(result.getCode());
        if(result.getCode() == 200){
            builder.setResult(result.getResult());
        }else{
            builder.setDesc(result.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogContentByPage(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.FindWblogContentByPageReq req = WblogContentProto.FindWblogContentByPageReq.parseFrom(param);
        WblogContentResult<List<WblogContentPojo>> wblogContentByPage;
        if(!StringUtils.isBlank(req.getCreator())){
            wblogContentByPage = wblogContentService.findWblogContentByPage(req.getPage(), req.getCreator(), req.getSize());
        }else{
            wblogContentByPage = wblogContentService.findAllWblogContentByPage(req.getPage(),req.getSize());
        }
        WblogContentProto.FindWblogContentByPageRes.Builder builder = WblogContentProto.FindWblogContentByPageRes.newBuilder();
        List<WblogContentPojo> mid = wblogContentByPage.getResult();
        builder.setCode(wblogContentByPage.getCode());
       if(wblogContentByPage.getCode() == 200) {
           for (int i = 0; i < mid.size(); i++) {
               WblogContentProto.WblogContentPojo.Builder wblogContentPojo = WblogContentProto.WblogContentPojo.newBuilder();
               wblogContentPojo.setId(mid.get(i).getId());
               wblogContentPojo.setImageUrl(mid.get(i).getImageUrl());
               Timestamp.Builder builder1 = Timestamp.newBuilder();
               builder1.setSeconds(mid.get(i).getCreateDate().getTime()/1000);
               wblogContentPojo.setCreateDate(builder1);
               wblogContentPojo.setContent(mid.get(i).getContent());
               wblogContentPojo.setCreator(mid.get(i).getCreator());
               builder.addResult(i, wblogContentPojo);
           }
       }else{
           builder.setDesc(wblogContentByPage.getDesc());
       }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] deleteWblog(byte[] param) throws InvalidProtocolBufferException {
        WblogContentProto.DeleteWblogContentReq req = WblogContentProto.DeleteWblogContentReq.parseFrom(param);
        WblogContentResult<Boolean> result = wblogContentService.deleteWblog(req.getId(), req.getNickName());
        WblogContentProto.DeleteWblogContentRes.Builder builder = WblogContentProto.DeleteWblogContentRes.newBuilder();
        builder.setCode(result.getCode());
        if(result.getCode()==200) {
            builder.setResult(result.getResult());
        }else {
            builder.setDesc(result.getDesc());
        }
        return builder.build().toByteArray();
    }
}
