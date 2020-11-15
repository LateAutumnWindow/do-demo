package com.yan.demo.ElasticSearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ESClient {
    public static RestHighLevelClient getClient() {
        HttpHost hh = new HttpHost("192.168.1.180", 9200);
        RestClientBuilder builder = RestClient.builder(hh);
        return new RestHighLevelClient(builder);
    }
}
