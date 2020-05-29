package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.wblog.content.service.WblogMsgService;
import com.wblog.pojo.WblogMsgPojo;
import com.wblog.proto.WblogMsgProto;
import com.weblog.content.common.WblogContentResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("WblogMsgRpc")
public class WblogMsgRpcImpl implements WblogMsgRpc{
    @Autowired
    WblogMsgService wblogMsgService;
    @Override
    public byte[] addWblogMsg(byte[] param) throws InvalidProtocolBufferException {
        WblogMsgProto.AddWblogMsgReq req = WblogMsgProto.AddWblogMsgReq.parseFrom(param);
        WblogMsgPojo wblogMsgPojo = new WblogMsgPojo();
        BeanUtils.copyProperties(req.getPojo(),wblogMsgPojo);
        WblogContentResult<Boolean> result = wblogMsgService.addWblogMsg(wblogMsgPojo);
        WblogMsgProto.AddWblogMsgRes.Builder builder = WblogMsgProto.AddWblogMsgRes.newBuilder();
        builder.setCode(result.getCode());
        if(result.getCode() == 200){
            builder.setResult(result.getResult());
        }else {
            builder.setDesc(result.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogContentByPage(byte[] param) throws InvalidProtocolBufferException {
        WblogMsgProto.FindWblogContentByPageReq req = WblogMsgProto.FindWblogContentByPageReq.parseFrom(param);
        WblogContentResult<List<WblogMsgPojo>> wblogContentByPage = wblogMsgService.findWblogContentByPage(req.getPage(), req.getCreator(), req.getSender(), req.getSize());
        List<WblogMsgPojo> mid = wblogContentByPage.getResult();
        WblogMsgProto.FindWblogContentByPageRes.Builder builder = WblogMsgProto.FindWblogContentByPageRes.newBuilder();
        builder.setCode(wblogContentByPage.getCode());
        if(wblogContentByPage.getCode() == 200) {
            for (int i = 1; i < mid.size(); i++) {
                WblogMsgProto.WblogMsgPojo.Builder wblogMsgPojo = WblogMsgProto.WblogMsgPojo.newBuilder();
                Timestamp.Builder builder1 = Timestamp.newBuilder();
                builder1.setSeconds(mid.get(i).getCreateDate().getTime()/1000);
                wblogMsgPojo.setCreateDate(builder1);
                wblogMsgPojo.setId(mid.get(i).getId());
                wblogMsgPojo.setMsgContent(mid.get(i).getMsgContent());
                wblogMsgPojo.setMsgSender(mid.get(i).getMsgSender());
                wblogMsgPojo.setMsgReceiver(mid.get(i).getMsgReceiver());
                wblogMsgPojo.setStatus(mid.get(i).getStatus());
                builder.addResult(i, wblogMsgPojo);
            }
        }else {
            builder.setDesc(wblogContentByPage.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] deleteMsg(byte[] param) throws InvalidProtocolBufferException {
       WblogMsgProto.DeleteMsgReq req = WblogMsgProto.DeleteMsgReq.parseFrom(param);
       WblogContentResult<Boolean> result = wblogMsgService.deleteMsg(req.getId(), req.getNickName());
        WblogMsgProto.DeleteMsgRes.Builder builder = WblogMsgProto.DeleteMsgRes.newBuilder();
        builder.setCode(result.getCode());
         builder.setDesc(result.getDesc());
         builder.setResult(result.getResult());
         return builder.build().toByteArray();
    }
}
