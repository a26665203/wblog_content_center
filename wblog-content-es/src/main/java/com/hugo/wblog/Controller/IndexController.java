package com.hugo.wblog.Controller;

import com.alibaba.fastjson.JSON;
import com.hugo.wblog.Util.ClientUtil;
import com.hugo.wblog.dao.TestDao;
import com.hugo.wblog.domain.Article;
import javafx.beans.binding.ObjectExpression;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.EsClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/index")
public class IndexController {
    @Autowired
    private TestDao articleDao;

    @RequestMapping("/article")
    @ResponseBody
    public Article getArticle(HttpServletRequest request) {
        String id = "1";
        return articleDao.findById(id).get();
    }
    @RequestMapping("/createIndex")
    @ResponseBody
    public CreateIndexResponse createIndex(@RequestParam String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("wblog_content_center");
        buildContentIndexMapping(request);
        CreateIndexResponse createIndexResponse = ClientUtil.getClient().indices().create(request);
        return createIndexResponse;
    }
    @RequestMapping("/createIndex/comment")
    @ResponseBody
    public CreateIndexResponse createCommentIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("wblog_content_center_comment");
        buildCommentIndexMapping(request);
        return ClientUtil.getClient().indices().create(request);
    }
    @RequestMapping("/createIndex/Msg")
    @ResponseBody
    public CreateIndexResponse createMsgIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("wblog_content_center_msg");
        buildMsgIndexMapping(request);
        CreateIndexResponse createIndexResponse = ClientUtil.getClient().indices().create(request);
        return createIndexResponse;
    }
    //微博内容类型
    private void buildContentIndexMapping(CreateIndexRequest request) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String,Object> id = new HashMap<>();//id
        id.put("type","integer");
        Map<String, Object> content = new HashMap<>();//正文
        content.put("type","text");
        Map<String,Object> imageUrl = new HashMap<>();//图片url
        imageUrl.put("type","text");
        Map<String, Object> creator = new HashMap<>();//创建者
        creator.put("type","text");
        Map<String,Object> createDate = new HashMap<>();//创建时间
        createDate.put("type","date");
        Map<String, Object> properties = new HashMap<>();
        properties.put("id",id);
        properties.put("content",content);
        properties.put("imageUrl",imageUrl);
        properties.put("creator",creator);
        properties.put("createDate",createDate);
        Map<String, Object> wblogContent = new HashMap<>();
        wblogContent.put("properties", properties);
        jsonMap.put("wblogContents", wblogContent);
        request.mapping("wblogContents", jsonMap);//类型名
    }
    private void buildCommentIndexMapping(CreateIndexRequest request) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String,Object> id = new HashMap<>();//id
        id.put("type","integer");
        Map<String, Object> commentContent = new HashMap<>();//评论正文
        commentContent.put("type","text");
        Map<String,Object> blogId = new HashMap<>();//微博id
        blogId.put("type","integer");
        Map<String, Object> commenter = new HashMap<>();//评论者
        commenter.put("type","text");
        Map<String,Object> createDate = new HashMap<>();//创建时间
        createDate.put("type","date");
        Map<String, Object> properties = new HashMap<>();
        properties.put("id",id);
        properties.put("commentContent",commentContent);
        properties.put("blogId",blogId);
        properties.put("commenter",commenter);
        properties.put("createDate",createDate);
        Map<String, Object> wblogComment = new HashMap<>();
        wblogComment.put("properties", properties);
        jsonMap.put("wblogComments", wblogComment);
        request.mapping("wblogComments", jsonMap);//类型名
    }
    private void buildMsgIndexMapping(CreateIndexRequest request) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String,Object> id = new HashMap<>();//id
        id.put("type","integer");
        Map<String, Object> msgSender = new HashMap<>();//消息发送者
        msgSender.put("type","text");
        Map<String,Object> msgReceiver = new HashMap<>();//消息接受者
        msgReceiver.put("type","text");
        Map<String, Object> msgContent = new HashMap<>();//消息正文
        msgContent.put("type","text");
        Map<String, Object> status = new HashMap<>();//消息状态，1为已发送，0为为发送
        status.put("type","text");
        Map<String,Object> createDate = new HashMap<>();//创建时间
        createDate.put("type","date");
        Map<String, Object> properties = new HashMap<>();
        properties.put("id",id);
        properties.put("msgSender",msgSender);
        properties.put("msgReceiver",msgReceiver);
        properties.put("msgContent",msgContent);
        properties.put("status",status);
        properties.put("createDate",createDate);
        Map<String, Object> wblogMsg = new HashMap<>();
        wblogMsg.put("properties", properties);
        jsonMap.put("wblogMsgs", wblogMsg);
        request.mapping("wblogMsgs", jsonMap);//类型名
    }
}