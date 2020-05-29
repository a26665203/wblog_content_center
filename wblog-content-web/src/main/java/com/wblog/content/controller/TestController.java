package com.wblog.content.controller;

import com.wblog.content.service.WblogContentService;
import com.wblog.pojo.WblogContentPojo;
import com.weblog.content.common.WblogContentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @Autowired
    WblogContentService wblogContentService;
    @RequestMapping("/findWblog")
    @ResponseBody
    public WblogContentResult<WblogContentPojo> findWblogById(String id){
        return wblogContentService.findWblogContentById(id);
    }
}
