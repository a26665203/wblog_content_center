package com.wblog.content.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
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
        if(result.getCode() == 200){
            builder.setResult(result.getResult());
        }else {
            builder.setDesc(result.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogCommentByPage(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.FindWblogCommentByPageReq req = WblogCommentProto.FindWblogCommentByPageReq.parseFrom(param);
        WblogContentResult<List<CommentContentPojo>> wblogCommentByPage = wblogCommentService.findWblogCommentByPage(req.getPage(), req.getCreator(), req.getSize());
        List<CommentContentPojo> result = wblogCommentByPage.getResult();
        WblogCommentProto.FindWblogCommentByPageRes.Builder builder = WblogCommentProto.FindWblogCommentByPageRes.newBuilder();
        builder.setCode(wblogCommentByPage.getCode());
        if(wblogCommentByPage.getCode() == 200) {

            for (int i = 0; i < result.size(); i++) {
                WblogCommentProto.CommentContentPojo.Builder builder1 = WblogCommentProto.CommentContentPojo.newBuilder();
                builder1.setBlogId(result.get(i).getBlogId());
                Timestamp.Builder builder2 = Timestamp.newBuilder();
                builder2.setSeconds(result.get(i).getCreateDate().getTime() / 1000);
                builder1.setCreateDate(builder2);
                builder1.setCommenter(result.get(i).getCommenter());
                builder1.setCommentContent(result.get(i).getCommentContent());
                builder1.setId(result.get(i).getId());
                builder.addResult(i, builder1);
            }
        }
        else {
            builder.setDesc(wblogCommentByPage.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] deleteWblogComment(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.DeleteWblogCommentReq req = WblogCommentProto.DeleteWblogCommentReq.parseFrom(param);
        WblogContentResult<Boolean> result = wblogCommentService.deleteWblogComment(req.getId(), req.getNickName());
        WblogCommentProto.DeleteWblogCommentRes.Builder builder = WblogCommentProto.DeleteWblogCommentRes.newBuilder();
        builder.setCode(result.getCode());
        if(result.getCode() == 200) {
            builder.setResult(result.getResult());
        }else {
            builder.setDesc(result.getDesc());
        }
        return builder.build().toByteArray();
    }

    @Override
    public byte[] findWblogCommentByBlogId(byte[] param) throws InvalidProtocolBufferException {
        WblogCommentProto.FindWblogCommentByBlogIdReq req = WblogCommentProto.FindWblogCommentByBlogIdReq.parseFrom(param);
        WblogContentResult<List<CommentContentPojo>> wblogCommentByBlogId = wblogCommentService.findWblogCommentByBlogId(req.getPage(), req.getBlogId(), req.getSize());
        List<CommentContentPojo> result = wblogCommentByBlogId.getResult();
        WblogCommentProto.FindWblogCommentByBlogIdRes.Builder builder = WblogCommentProto.FindWblogCommentByBlogIdRes.newBuilder();
        builder.setCode(wblogCommentByBlogId.getCode());
        if(wblogCommentByBlogId.getCode() == 200) {
            for (int i = 0; i < result.size(); i++) {
                WblogCommentProto.CommentContentPojo.Builder builder1 = WblogCommentProto.CommentContentPojo.newBuilder();
                builder1.setBlogId(result.get(i).getBlogId());
                Timestamp.Builder builder2 = Timestamp.newBuilder();
                builder2.setSeconds(result.get(i).getCreateDate().getTime() / 1000);
                builder1.setCreateDate(builder2);
                builder1.setCommenter(result.get(i).getCommenter());
                builder1.setCommentContent(result.get(i).getCommentContent());
                builder1.setId(result.get(i).getId());
                builder.addResult(i, builder1);
            }
        }else{
            builder.setDesc(wblogCommentByBlogId.getDesc());
        }
        return builder.build().toByteArray();
    }
}
