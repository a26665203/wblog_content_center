package com.wblog.content.service;

import com.wblog.pojo.CommentContentPojo;
import com.wblog.pojo.WblogContentPojo;
import com.weblog.content.common.WblogContentResult;

import java.util.List;

public interface WblogCommentService {
    public WblogContentResult<Boolean> addWblogComment(WblogContentPojo wblogContentPojo);
    public WblogContentResult<List<CommentContentPojo>> findWblogCommentByPage(Integer page, String creator, Integer size);
    public WblogContentResult<Boolean> deleteWblogComment(String id,String nickName);
}
