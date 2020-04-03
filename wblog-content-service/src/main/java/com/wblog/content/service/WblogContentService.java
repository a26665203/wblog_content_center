package com.wblog.content.service;

import com.wblog.pojo.WblogContentPojo;
import com.wblog.proto.WblogContentProto;
import com.weblog.content.common.WblogContentResult;

import java.util.List;

public interface WblogContentService {
    public WblogContentResult<Boolean> addWblogContent(WblogContentPojo wblogContentPojo);
    public WblogContentResult<List<WblogContentPojo>> findWblogContentByPage(Integer page,String creator,Integer size);
    public WblogContentResult<Boolean> deleteWblog(String id,String nickName);
}
