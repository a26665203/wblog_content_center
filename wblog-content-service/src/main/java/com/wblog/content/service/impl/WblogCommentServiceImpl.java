package com.wblog.content.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wblog.content.esClient.ContentESClient;
import com.wblog.content.service.WblogCommentService;
import com.wblog.pojo.CommentContentPojo;
import com.wblog.pojo.WblogContentPojo;
import com.wblog.proto.DecrUserAboutProto;
import com.wblog.proto.IncrUserAboutProto;
import com.wblog.user.rpc.WblogUserAboutRpc;
import com.weblog.content.common.WblogContentResult;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

public class WblogCommentServiceImpl implements WblogCommentService {
    Logger logger = LoggerFactory.getLogger(WblogCommentServiceImpl.class);

    @Resource
    WblogUserAboutRpc wblogUserAboutRpc;
    @Override
    public WblogContentResult<Boolean> addWblogComment(WblogContentPojo wblogContentPojo) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        logger.info("WblogCommentServiceImpl.addWblogComment");
        if(wblogContentPojo==null){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        if(StringUtils.isBlank(wblogContentPojo.getCreator())){
            result.setCode(400);
            result.setDesc("评论创建者不能为空");
            return result;
        }
        try{
            wblogContentPojo.setId(UUID.randomUUID().toString());
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(wblogContentPojo));
            ContentESClient.saveData(jsonObject,"wblog_content_center_comment","wblogComments");
            IncrUserAboutProto.incrUserAboutReq.Builder builder = IncrUserAboutProto.incrUserAboutReq.newBuilder();
            builder.setReqName("comment");
            builder.setNickName(wblogContentPojo.getCreator());
            byte[] bytes = wblogUserAboutRpc.incrReq(builder.build().toByteArray());
            IncrUserAboutProto.incrUserAboutRes res = IncrUserAboutProto.incrUserAboutRes.parseFrom(bytes);
            if(res.getCode()==200){
                result.setCode(200);
                result.setResult(true);
            }else{
                result.setCode(res.getCode());
                result.setDesc(res.getDesc());
            }
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.addWblogComment----->"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<List<CommentContentPojo>> findWblogCommentByPage(Integer page, String creator, Integer size) {
        WblogContentResult<List<CommentContentPojo>> result = new WblogContentResult<List<CommentContentPojo>>();
        logger.info("WblogCommentServiceImpl.findWblogCommentByPage------>creator:"+creator+",page:"+page+",size:"+size);
        try{
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("creator",creator));
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center_comment")
                    .setTypes("wblogComments").setQuery(boolQueryBuilder).addSort("createDate", SortOrder.DESC).setSize(size).setFrom(page)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<CommentContentPojo> list = new ArrayList<CommentContentPojo>();
            for(SearchHit hit : hits.getHits()){
                CommentContentPojo commentContentPojo = new CommentContentPojo();
                Map<String, DocumentField> fields = hit.getFields();
                commentContentPojo.setId((String)fields.get("id").getValue());
                commentContentPojo.setBlogId((String)fields.get("blogId").getValue());
                commentContentPojo.setCommentContent((String) fields.get("commentContent").getValue());
                commentContentPojo.setCommenter((String)fields.get("commenter").getValue());
                commentContentPojo.setCreateDate((Date)fields.get("createDate").getValue());
                list.add(commentContentPojo);
            }
            result.setCode(200);
            result.setResult(list);
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.findWblogCommentByPage.error---->"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<Boolean> deleteWblogComment(String id, String nickName) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        if(StringUtils.isBlank(id)){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        try{
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("id",id));
            BulkByScrollResponse res = DeleteByQueryAction.INSTANCE.newRequestBuilder(ContentESClient.getClient())
                    .filter(boolQueryBuilder).source("wblog_content_center_comment","wblogComments").get();
            long del = res.getDeleted();
            DecrUserAboutProto.decrUserAboutReq.Builder builder = DecrUserAboutProto.decrUserAboutReq.newBuilder();
            builder.setNickName(nickName);
            builder.setReqName("comment");
            byte[] bytes = wblogUserAboutRpc.decrReq(builder.build().toByteArray());
            DecrUserAboutProto.decrUserAboutRes response = DecrUserAboutProto.decrUserAboutRes.parseFrom(bytes);
            Integer code = response.getCode();
            if(code == 200){
                result.setCode(200);
                result.setResult(true);
            }else{
                result.setCode(response.getCode());
                result.setDesc(response.getDesc());
            }
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.deleteWblogComment------>"+e);
        }
        return result;
    }
}
