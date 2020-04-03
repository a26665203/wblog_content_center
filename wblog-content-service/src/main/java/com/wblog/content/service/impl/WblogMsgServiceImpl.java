package com.wblog.content.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wblog.content.esClient.ContentESClient;
import com.wblog.content.service.WblogMsgService;
import com.wblog.pojo.WblogMsgPojo;
import com.wblog.user.rpc.WblogUserAboutRpc;
import com.weblog.content.common.WblogContentResult;
import org.elasticsearch.action.search.SearchRequestBuilder;
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

public class WblogMsgServiceImpl implements WblogMsgService {
    Logger logger = LoggerFactory.getLogger(WblogMsgServiceImpl.class);
    @Override
    public WblogContentResult<Boolean> addWblogMsg(WblogMsgPojo wblogContentPojo) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        logger.info("WblogMsgServiceImpl.addWblogMsg----->"+ JSON.toJSONString(wblogContentPojo));
        if(wblogContentPojo == null){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        if(StringUtils.isBlank(wblogContentPojo.getMsgSender())){
            result.setCode(400);
            result.setDesc("缺乏消息发送者");
            return result;
        }
        if(StringUtils.isBlank(wblogContentPojo.getMsgReceiver())){
            result.setCode(400);
            result.setDesc("缺乏消息接收者");
            return result;
        }
        try{
            wblogContentPojo.setId(UUID.randomUUID().toString());
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(wblogContentPojo));
            ContentESClient.saveData(jsonObject,"wblog_content_center_msg","wblogMsgs");
            //应该还有个发送消息的逻辑，后面补
            result.setCode(200);
            result.setResult(true);
        }catch (Exception e){
            logger.error("WblogMsgServiceImpl.addWblogMsg.error------>"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }

    @Override
    public WblogContentResult<List<WblogMsgPojo>> findWblogContentByPage(Integer page, String creator,String sender, Integer size) {
        WblogContentResult<List<WblogMsgPojo>> result = new WblogContentResult<List<WblogMsgPojo>>();
        logger.info("WblogMsgServiceImpl.findWblogContentByPage---->page:"+page+",");
        try{
            BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
            boolQueryBuilder1.must(QueryBuilders.termQuery("msgSender",creator));
            boolQueryBuilder1.must(QueryBuilders.termQuery("msgReceiver",sender));
            BoolQueryBuilder builder2 = QueryBuilders.boolQuery();
            builder2.must(QueryBuilders.termQuery("msgSender",sender));
            builder2.must(QueryBuilders.termQuery("msgReceiver",creator));
            BoolQueryBuilder finalBuilder = QueryBuilders.boolQuery();
            finalBuilder.should(boolQueryBuilder1);
            finalBuilder.should(builder2);
            SearchRequestBuilder searchRequestBuilder = ContentESClient.getClient().prepareSearch("wblog_content_center_msg");
            searchRequestBuilder.setPostFilter(finalBuilder);
            searchRequestBuilder.setTypes("wblogMsgs").setQuery(finalBuilder).addSort("createDate", SortOrder.DESC).setFrom(page).setSize(size);
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            SearchHits searchHits = response.getHits();
            List<WblogMsgPojo> res = new ArrayList<WblogMsgPojo>();
            for(SearchHit hit : searchHits){
                WblogMsgPojo wblogMsgPojo = new WblogMsgPojo();
                Map<String, DocumentField> fieldMap = hit.getFields();
                wblogMsgPojo.setId((String) fieldMap.get("id").getValue());
                wblogMsgPojo.setCreateDate((Date) fieldMap.get("createDate").getValue());
                wblogMsgPojo.setMsgContent((String) fieldMap.get("msgContent").getValue());
                wblogMsgPojo.setMsgReceiver((String) fieldMap.get("msgReceiver").getValue());
                wblogMsgPojo.setMsgSender((String) fieldMap.get("msgSender").getValue());
                wblogMsgPojo.setStatus((Integer) fieldMap.get("status").getValue());
                res.add(wblogMsgPojo);
            }
            result.setResult(res);
            result.setCode(200);
        }catch (Exception e){
            logger.error("WblogMsgService.findWblogContentByPage,{}",e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }


    @Override
    public WblogContentResult<Boolean> deleteMsg(String id, String nickName) {
        WblogContentResult<Boolean> result = new WblogContentResult<Boolean>();
        if(StringUtils.isBlank(id)||StringUtils.isBlank(nickName)){
            result.setCode(400);
            result.setDesc("参数错误");
            return result;
        }
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("id", id));
            BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(ContentESClient.getClient())
                    .filter(boolQueryBuilder).source("wblog_content_center_msg", "wblogMsgs").get();
            long del = response.getDeleted();
            result.setCode(200);
            result.setResult(true);
        }catch (Exception e){
            logger.error("WblogMsgServiceImpl.deleteMsg.error----->"+e);
            result.setCode(400);
            result.setDesc(e.getMessage());
        }
        return result;
    }
}
