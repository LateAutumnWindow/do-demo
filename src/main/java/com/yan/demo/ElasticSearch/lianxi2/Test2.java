package com.yan.demo.ElasticSearch.lianxi2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.demo.ElasticSearch.ESClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test2 {

    ObjectMapper om = new ObjectMapper();
    RestHighLevelClient client = ESClient.getClient();
    String index = "address_index";
    String type = "address_type";

    @Test
    public void sea() throws IOException {

        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.matchQuery("addrName", "北京 南京").operator(Operator.OR));

        sr.source(ssb);
        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }

    }

    @Test
    public void se() throws IOException {

        DeleteIndexRequest dir = new DeleteIndexRequest();
        dir.indices(index);

        AcknowledgedResponse delete = client.indices().delete(dir, RequestOptions.DEFAULT);
        System.out.println(delete);

    }

    @Test
    public void ssk() throws IOException {
        String km = "经济师考试酷狗阿斯顿发水电站总资产VB你们回家吗，即，接口，好久没规划局玫瑰花aasd";

        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.matchQuery("desc", km).operator(Operator.AND));

        sr.source(ssb);


        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getId());
        }
    }


    @Test
    public void seInd () throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("nid", "10002");
        map.put("addrName", "南京");
        map.put("addZb", "12222,33332");
        map.put("desc", "经济师bxzcvb撒旦法手动阀是打发斯蒂芬熊出没用户界面好几个玫瑰花aasd");


        IndexRequest ir = new IndexRequest(index, type);
        ir.source(map);

        IndexResponse index = client.index(ir, RequestOptions.DEFAULT);
        System.out.println(index.toString());
    }

    @Test
    public void createIndex() throws IOException {

        Settings settings = Settings.builder()
                .put("number_of_shards", 4)
                .put("number_of_replicas", 1)
                .build();


        XContentBuilder xContentBuilder = JsonXContent.contentBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("nid")
                            .field("type", "keyword")
                        .endObject()
                        .startObject("addrName")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                        .endObject()
                        .startObject("addrZb")
                            .field("type", "geo_point")
                        .endObject()
                        .startObject("desc")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                        .endObject()
                    .endObject()
                .endObject();


        CreateIndexRequest cir = new CreateIndexRequest(index);
        cir.settings(settings);
        cir.mapping(type, xContentBuilder);


        CreateIndexResponse resp = client.indices().create(cir, RequestOptions.DEFAULT);
        System.out.println(resp.toString());

    }

}
