package com.wblog.content.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wblog.content.esClient.ContentESClient;
import com.wblog.content.service.WblogContentService;
import com.wblog.pojo.WblogContentPojo;
import com.wblog.proto.DecrUserAboutProto;
import com.wblog.proto.IncrUserAboutProto;
import com.wblog.user.rpc.WblogUserAboutRpc;
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
    @Resource
    WblogUserAboutRpc wblogUserAboutRpc;
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
            wblogContentPojo.setId(UUID.randomUUID()+"");
            //先添加到es，再把微博数+1
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(wblogContentPojo));
            ContentESClient.saveData(jsonObject,"wblog_content_center","wblogContents");
            //调用用户中心，添加微博数
            IncrUserAboutProto.incrUserAboutReq.Builder builder = IncrUserAboutProto.incrUserAboutReq.newBuilder();
            builder.setNickName(wblogContentPojo.getCreator());
            builder.setReqName("wblog");
            byte[] bytes = wblogUserAboutRpc.incrReq(builder.build().toByteArray());
            IncrUserAboutProto.incrUserAboutRes res = IncrUserAboutProto.incrUserAboutRes.parseFrom(bytes);
            int code = res.getCode();
            if("200".equals(code)){
                result.setCode(200);
                result.setResult(true);
            }else{
                result.setCode(code);
                result.setDesc(res.getDesc());
            }
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
            boolQueryBuilder.must(QueryBuilders.termQuery("creator", creator));
            SearchResponse response = ContentESClient.getClient().prepareSearch("wblog_content_center").setTypes("wblogContents")
                    .setQuery(boolQueryBuilder).addSort("createDate", SortOrder.DESC).setFrom(page).setSize(size)
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            List<WblogContentPojo> result = new ArrayList<WblogContentPojo>();
            for (SearchHit hit : hits.getHits()) {
                WblogContentPojo wblogContentPojo = new WblogContentPojo();
                Map<String, DocumentField> fields = hit.getFields();
                wblogContentPojo.setId((String) fields.get("id").getValue());
                wblogContentPojo.setContent((String) fields.get("content").getValue());
                wblogContentPojo.setCreator((String) fields.get("creator").getValue());
                wblogContentPojo.setImageUrl((String) fields.get("imageUrl").getValue());
                wblogContentPojo.setCreateDate((Date) fields.get("createDate").getValue());
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
                    .filter(queryBuilder).source("wblog_content_center","wblogContents").get();
            long del = res.getDeleted();
            DecrUserAboutProto.decrUserAboutReq.Builder builder = DecrUserAboutProto.decrUserAboutReq.newBuilder();
            builder.setReqName("wblog");
            builder.setNickName(nickName);
            byte[] bytes = wblogUserAboutRpc.decrReq(builder.build().toByteArray());
            DecrUserAboutProto.decrUserAboutRes resq = DecrUserAboutProto.decrUserAboutRes.parseFrom(bytes);
            if (resq.getCode() == 200) {
                result.setCode(200);
                result.setResult(true);
            } else {
                result.setCode(resq.getCode());
                result.setDesc(resq.getDesc());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("WblogContentServiceImpl.deleteWblog----->" + e);
        }
        return result;
    }
}
