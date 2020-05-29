package com.wblog.content.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wblog.content.esClient.ContentESClient;
import com.wblog.content.service.WblogCommentService;
import com.wblog.pojo.CommentContentPojo;
import com.wblog.pojo.WblogContentPojo;
import com.weblog.content.common.IDUtils;
import com.weblog.content.common.WblogContentResult;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
@Service
public class WblogCommentServiceImpl implements WblogCommentService {
    Logger logger = LoggerFactory.getLogger(WblogCommentServiceImpl.class);

    @Override
    public WblogContentResult<Boolean> addWblogComment(CommentContentPojo commentContentPojo) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        logger.info("WblogCommentServiceImpl.addWblogComment");
        if(commentContentPojo==null){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        if(StringUtils.isBlank(commentContentPojo.getCommenter())){
            result.setCode(400);
            result.setDesc("评论创建者不能为空");
            return result;
        }
        try{
            commentContentPojo.setCreateDate(new Date());
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(commentContentPojo));
            ContentESClient.saveData(jsonObject,"wblog_content_center_comment","wblogComments");
            result.setCode(200);
            result.setResult(true);
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.addWblogComment----->"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<List<CommentContentPojo>> findWblogCommentByPage(Integer page, String commenter, Integer size) {
        WblogContentResult<List<CommentContentPojo>> result = new WblogContentResult<List<CommentContentPojo>>();
        logger.info("WblogCommentServiceImpl.findWblogCommentByPage------>commenter:"+commenter+",page:"+page+",size:"+size);
        try{
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("commenter",commenter));
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center_comment")
                    .setTypes("wblogComments").setQuery(boolQueryBuilder).addSort("createDate", SortOrder.DESC).setSize(size).setFrom(page)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<CommentContentPojo> list = new ArrayList<CommentContentPojo>();
            for(SearchHit hit : hits.getHits()){
                CommentContentPojo commentContentPojo = new CommentContentPojo();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                commentContentPojo.setId((String) sourceAsMap.get("id"));
                commentContentPojo.setBlogId((String) sourceAsMap.get("blogId"));
                commentContentPojo.setCommentContent((String) sourceAsMap.get("commentContent"));
                commentContentPojo.setCommenter((String) sourceAsMap.get("commenter"));
                Long time = (Long) sourceAsMap.get("createDate");
                commentContentPojo.setCreateDate(new Date(time));;
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
        if(id==null){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        try{
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("id",id));
            BulkByScrollResponse res = DeleteByQueryAction.INSTANCE.newRequestBuilder(ContentESClient.getClient())
                    .filter(boolQueryBuilder).source("wblog_content_center_comment").get();
//            DeleteResponse deleteResponse = ContentESClient.getClient().prepareDelete().setIndex("wblog_content_center_comment").setType("wblogComments").setId(id).execute().actionGet();
//            logger.info("success or not------> );
            result.setCode(200);
            result.setResult(true);
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.deleteWblogComment------>"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<List<CommentContentPojo>> findWblogCommentByBlogId(Integer page, String blogId, Integer size) {
        WblogContentResult<List<CommentContentPojo>> result = new WblogContentResult<List<CommentContentPojo>>();
        logger.info("WblogCommentServiceImpl.findWblogCommentByBlogId----->page:"+page+",blogId:"+blogId);
        try{
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("blogId",blogId));
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center_comment").setTypes("wblogComments")
                    .setQuery(boolQueryBuilder).addSort("createDate",SortOrder.DESC).setFrom(page).setSize(size).execute().actionGet();
            SearchHits hits = response.getHits();
            List<CommentContentPojo> list = new ArrayList<CommentContentPojo>();
            for(SearchHit hit : hits.getHits()){
                CommentContentPojo commentContentPojo = new CommentContentPojo();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                commentContentPojo.setId((String) sourceAsMap.get("id"));
                commentContentPojo.setBlogId((String) sourceAsMap.get("blogId"));
                commentContentPojo.setCommentContent((String) sourceAsMap.get("commentContent"));
                commentContentPojo.setCommenter((String) sourceAsMap.get("commenter"));
                Long time = (Long) sourceAsMap.get("createDate");
                commentContentPojo.setCreateDate(new Date(time));
                list.add(commentContentPojo);
            }
            result.setCode(200);
            result.setResult(list);
        }catch (Exception e){
            logger.error("WblogCommentServiceImpl.findWBlogCommentByBlogId.{}",e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }
}
