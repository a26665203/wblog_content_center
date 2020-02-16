package com.hugo.wblog.Util;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.settings.Settings;

public class ClientUtil {
    private static RestHighLevelClient client;
    public static RestHighLevelClient getClient(){
        if(client == null) {
            synchronized (ClientUtil.class) {
                if(client == null){
                    client = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200,"http")));
                }
            }
        }
        return client;
    }
}
