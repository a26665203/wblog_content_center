package com.wblog.content.esClient;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ContentESClient {
    public static TransportClient transportClient;
    static {
        Settings settings = Settings.builder().put("cluster.name","elasticsearch").build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            TransportAddress address = new TransportAddress(InetAddress.getByName("127.0.0.1"),9300);
            transportClient.addTransportAddress(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public static TransportClient getClient(){
        return transportClient;
    }

    //创建索引
    public static void createIndex(String indexName) throws ExecutionException, InterruptedException {
        transportClient.admin().indices().prepareCreate(indexName).execute().get();
    }

    //删除索引
    public static void deleteIndex(String indexName){
        transportClient.admin().indices().prepareDelete(indexName).execute().actionGet();
    }
    //保存数据，json数据
    public static IndexResponse saveData(JSONObject object,String indexName,String typeName){
        XContentType xContentType = XContentType.JSON;
        IndexResponse response = transportClient.prepareIndex(indexName,typeName).setSource(object,xContentType).get();
        return response;
    }
    //批量导入数据
    public static BulkResponse batchSave(List<Object> objects,String indexName,String typeName) throws IllegalAccessException {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        for(Object obj:objects){
            Map map = obj2Map(obj);
            bulkRequestBuilder.add(transportClient.prepareIndex(indexName,typeName).setSource(map));
        }
        return bulkRequestBuilder.get();
    }
    public static Map<String,Object> obj2Map(Object obj) throws IllegalAccessException {
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
            map.put(field.getName(),field.get(obj));
        }
        return map;
    }


}
