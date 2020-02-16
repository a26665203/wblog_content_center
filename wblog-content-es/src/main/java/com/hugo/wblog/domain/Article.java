package com.hugo.wblog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "blog",type = "article", shards = 1,replicas = 0, refreshInterval = "-1")
public class Article {
    @Id
    private String id;
    private String title;
    private String content;
    private String[] newcontent;
    private String about;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getNewcontent() {
        return newcontent;
    }

    public void setNewcontent(String[] newcontent) {
        this.newcontent = newcontent;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
