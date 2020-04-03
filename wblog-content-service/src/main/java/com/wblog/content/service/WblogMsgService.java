package com.wblog.content.service;

import com.wblog.pojo.WblogContentPojo;
import com.wblog.pojo.WblogMsgPojo;
import com.weblog.content.common.WblogContentResult;

import java.util.List;

public interface WblogMsgService {
    public WblogContentResult<Boolean> addWblogMsg(WblogMsgPojo wblogContentPojo);

    public WblogContentResult<List<WblogMsgPojo>> findWblogContentByPage(Integer page, String creator,String sender, Integer size);
    public WblogContentResult<Boolean> deleteMsg(String id,String nickName);
}
