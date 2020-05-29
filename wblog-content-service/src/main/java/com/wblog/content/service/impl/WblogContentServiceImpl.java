package com.wblog.content.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wblog.content.esClient.ContentESClient;
import com.wblog.content.service.WblogContentService;
import com.wblog.pojo.WblogContentPojo;
import com.weblog.content.common.IDUtils;
import com.weblog.content.common.WblogContentResult;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.BulkIndexByScrollResponseContentListener;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class WblogContentServiceImpl implements WblogContentService {
    Logger logger = LoggerFactory.getLogger(WblogContentServiceImpl.class);
    @Override
    public WblogContentResult<Boolean> addWblogContent(WblogContentPojo wblogContentPojo) {
        logger.info("WblogContentServiceImpl.addWblogContent------>"+ JSON.toJSONString(wblogContentPojo));
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        if(wblogContentPojo == null){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        if(StringUtils.isBlank(wblogContentPojo.getCreator())){
            result.setCode(400);
            result.setDesc("微博创建者为空");
            return result;
        }
        try{
            wblogContentPojo.setCreateDate(new Date());
            //先添加到es，再把微博数+1
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(wblogContentPojo));
            ContentESClient.saveData(jsonObject,"wblog_content_center","wblogContents");
            result.setCode(200);
            result.setResult(true);
        }catch (Exception e){
            logger.error("WblogContentServiceImpl.addWblogContent------>"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    public WblogContentResult<List<WblogContentPojo>> findWblogContentByPage(Integer page, String creator, Integer size) {
        WblogContentResult<List<WblogContentPojo>> res = new WblogContentResult<List<WblogContentPojo>>();
        logger.info("WblogContentResult.findWblogContentByPage----->page:"+page+",creator:"+creator+",size:"+size);
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termsQuery("creator", creator));
            //中文分词
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center").setTypes("wblogContents")
                    .setQuery(boolQueryBuilder).addSort("createDate", SortOrder.DESC).setFrom(page).setSize(size)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<WblogContentPojo> result = new ArrayList<WblogContentPojo>();
            for (SearchHit hit : hits.getHits()) {
                WblogContentPojo wblogContentPojo = new WblogContentPojo();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                wblogContentPojo.setId((String) sourceAsMap.get("id"));
                wblogContentPojo.setContent((String) sourceAsMap.get("content"));
                wblogContentPojo.setCreator((String) sourceAsMap.get("creator"));
                wblogContentPojo.setImageUrl((String) sourceAsMap.get("imageUrl"));
                wblogContentPojo.setCreateDate(new Date((Long)sourceAsMap.get("createDate")));
                result.add(wblogContentPojo);
            }
            res.setCode(200);
            res.setResult(result);
        }catch (Exception e){
            logger.error("WblogContentResult.findWblogContentByPage,{}",e);
            res.setCode(400);
            res.setDesc(e.getMessage());
        }
        return res;
    }
    @Override
    public WblogContentResult<Boolean> deleteWblog(String id,String nickName) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        if (id == null) {
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termQuery("id", id));
            BulkByScrollResponse res = DeleteByQueryAction.INSTANCE.newRequestBuilder(ContentESClient.getClient())
                    .filter(queryBuilder).source("wblog_content_center").get();
            result.setCode(200);
            result.setResult(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("WblogContentServiceImpl.deleteWblog----->" + e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<List<WblogContentPojo>> findAllWblogContentByPage(Integer page, Integer size) {
        WblogContentResult<List<WblogContentPojo>> res = new WblogContentResult<List<WblogContentPojo>>();
        logger.info("WblogContentResult.findWblogContentByPage----->page:"+page+",size:"+size);
        try {
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center").setTypes("wblogContents")
                    .addSort("createDate", SortOrder.DESC).setFrom(page).setSize(size)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<WblogContentPojo> result = new ArrayList<WblogContentPojo>();
            for (SearchHit hit : hits.getHits()) {
                WblogContentPojo wblogContentPojo = new WblogContentPojo();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                wblogContentPojo.setId((String) sourceAsMap.get("id"));
                wblogContentPojo.setContent((String) sourceAsMap.get("content"));
                wblogContentPojo.setCreator((String) sourceAsMap.get("creator"));
                wblogContentPojo.setImageUrl((String) sourceAsMap.get("imageUrl"));
                wblogContentPojo.setCreateDate(new Date((Long)sourceAsMap.get("createDate")));
                result.add(wblogContentPojo);
            }
            res.setCode(200);
            res.setResult(result);
        }catch (Exception e){
            logger.error("WblogContentResult.findWblogContentByPage,{}",e);
            res.setCode(400);
            res.setDesc(e.getMessage());
        }
        return res;
    }

    @Override
    public WblogContentResult<WblogContentPojo> findWblogContentById(String id) {
        WblogContentResult<WblogContentPojo> res = new WblogContentResult<WblogContentPojo>();
        logger.info("WblogContentResult.findWblogContentById----->Id:"+id);
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("id", id));
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center").setTypes("wblogContents")
                    .setQuery(boolQueryBuilder).addSort("createDate", SortOrder.DESC)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<WblogContentPojo> result = new ArrayList<WblogContentPojo>();
            for (SearchHit hit : hits.getHits()) {
                WblogContentPojo wblogContentPojo = new WblogContentPojo();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                wblogContentPojo.setId((String) sourceAsMap.get("id"));
                wblogContentPojo.setContent((String) sourceAsMap.get("content"));
                wblogContentPojo.setCreator((String) sourceAsMap.get("creator"));
                wblogContentPojo.setImageUrl((String) sourceAsMap.get("imageUrl"));
                wblogContentPojo.setCreateDate(new Date((Long)sourceAsMap.get("createDate")));
                result.add(wblogContentPojo);
            }
            res.setCode(200);
            res.setResult(result.get(0));
        }catch (Exception e){
            logger.error("WblogContentResult.findWblogContentByPage,{}",e);
            res.setCode(400);
            res.setDesc(e.getMessage());
        }
        return res;
    }
}
