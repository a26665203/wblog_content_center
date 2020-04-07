package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wblog.content.service.WblogCommentService;
import com.wblog.pojo.CommentContentPojo;
import com.wblog.proto.CommentContentProto;
import com.wblog.proto.WblogCommentProto;
import com.weblog.content.common.WblogContentResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("WblogCommentRpc")
public class WblogCommentRpcImpl implements WblogCommentRpc {

    @Autowired
    WblogCommentService wblogCommentService;

    @Override
    public byte[] addWblogComment(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.AddWblogCommentReq addWblogCommentReq = WblogCommentProto.AddWblogCommentReq.parseFrom(param);
        CommentContentPojo pojo = new CommentContentPojo();
        BeanUtils.copyProperties(addWblogCommentReq.getPojo(),pojo);
        WblogContentResult<Boolean> result = wblogCommentService.addWblogComment(pojo);
        WblogCommentProto.AddWblogCommentRes.Builder builder = WblogCommentProto.AddWblogCommentRes.newBuilder();
        builder.setCode(result.getCode());
        builder.setDesc(result.getDesc());
        builder.setResult(result.getResult());
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogCommentByPage(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.FindWblogCommentByPageReq req = WblogCommentProto.FindWblogCommentByPageReq.parseFrom(param);
        WblogContentResult<List<CommentContentPojo>> wblogCommentByPage = wblogCommentService.findWblogCommentByPage(req.getPage(), req.getCreator(), req.getSize());
        List<CommentContentPojo> result = wblogCommentByPage.getResult();
        WblogCommentProto.FindWblogCommentByPageRes.Builder builder = WblogCommentProto.FindWblogCommentByPageRes.newBuilder();
        builder.setCode(wblogCommentByPage.getCode());
        builder.setDesc(wblogCommentByPage.getDesc());
        for(int i = 0 ;i<result.size();i++){
            WblogCommentProto.CommentContentPojo m = WblogCommentProto.CommentContentPojo.newBuilder().build();
            BeanUtils.copyProperties(result.get(i),m);
            builder.setResult(i,m);
        }

        return builder.build().toByteArray();
    }

    @Override
    public byte[] deleteWblogComment(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.DeleteWblogCommentReq req = WblogCommentProto.DeleteWblogCommentReq.parseFrom(param);
        WblogContentResult<Boolean> result = wblogCommentService.deleteWblogComment(req.getId(), req.getNickName());
        WblogCommentProto.DeleteWblogCommentRes.Builder builder = WblogCommentProto.DeleteWblogCommentRes.newBuilder();
        builder.setCode(result.getCode());
        builder.setDesc(result.getDesc());
        builder.setResult(result.getResult());
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogCommentByBlogId(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.FindWblogCommentByBlogIdReq req = WblogCommentProto.FindWblogCommentByBlogIdReq.parseFrom(param);
        WblogContentResult<List<CommentContentPojo>> wblogCommentByBlogId = wblogCommentService.findWblogCommentByBlogId(req.getPage(), req.getBlogId(), req.getSize());
        List<CommentContentPojo> result = wblogCommentByBlogId.getResult();
        WblogCommentProto.FindWblogCommentByBlogIdRes.Builder builder = WblogCommentProto.FindWblogCommentByBlogIdRes.newBuilder();
        builder.setCode(wblogCommentByBlogId.getCode());
        builder.setDesc(wblogCommentByBlogId.getDesc());
        for(int i = 0 ;i<result.size();i++){
            WblogCommentProto.CommentContentPojo m = WblogCommentProto.CommentContentPojo.newBuilder().build();
            BeanUtils.copyProperties(result.get(i),m);
            builder.setResult(i,m);
        }

        return builder.build().toByteArray();
    }
}
