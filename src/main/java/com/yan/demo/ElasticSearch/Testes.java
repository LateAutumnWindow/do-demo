package com.yan.demo.ElasticSearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Testes {
    ObjectMapper om = new ObjectMapper();
    RestHighLevelClient client = ESClient.getClient();
    String index = "person";
    String type = "man";

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 12:36
     *  @Description: 批量删除文档
     */
    @Test
    public void bulkDelDock() throws IOException {
        // 创建批量请求
        BulkRequest br = new BulkRequest();
        br.add(new DeleteRequest(index, type, "1001"));
        br.add(new DeleteRequest(index, type, "1002"));
        br.add(new DeleteRequest(index, type, "1003"));

        // 执行批量请求
        BulkResponse bulk = client.bulk(br, RequestOptions.DEFAULT);

        System.out.println(bulk);
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 12:31
     *  @Description: 批量请求数据
     */
    @Test
    public void bulkDock() throws IOException {
        // 创建多个数据
        Person p1 = new Person(1001, "JKl", 12, new Date());
        Person p2 = new Person(1002, "Jui", 13, new Date());
        Person p3 = new Person(1003, "Jza", 14, new Date());

        // 转换JSON
        String s1 = om.writeValueAsString(p1);
        String s2 = om.writeValueAsString(p2);
        String s3 = om.writeValueAsString(p3);

        // 添加到批量请求中
        BulkRequest br = new BulkRequest();
        br.add(new IndexRequest(index, type, p1.getId().toString()).source(s1, XContentType.JSON));
        br.add(new IndexRequest(index, type, p2.getId().toString()).source(s2, XContentType.JSON));
        br.add(new IndexRequest(index, type, p3.getId().toString()).source(s3, XContentType.JSON));

        BulkResponse bulk = client.bulk(br, RequestOptions.DEFAULT);
        System.out.println(bulk);
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 12:18
     *  @Description: 删除文档
     */
    @Test
    public void delDoc() throws IOException {
        DeleteRequest dr = new DeleteRequest(index, type, "19");

        DeleteResponse delete = client.delete(dr, RequestOptions.DEFAULT);

        System.out.println(delete.toString());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 12:13
     *  @Description: 更新数据
     */
    @Test
    public void update() throws IOException {
        Map<String, Object> map = new HashMap<>(2);
        map.put("name", "龙口");

        UpdateRequest ur = new UpdateRequest(index, type, "19");
        ur.doc(map);

        UpdateResponse update = client.update(ur, RequestOptions.DEFAULT);
        System.out.println(update.toString());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/23 17:13
     *  @Description: 创建一个文档数据
     */
    @Test
    public void insertDoc() throws IOException {
        //1. 创建要上传的对象（需要转换成Json）
        Person person = new Person(12, "张三", 22, new Date());
        String json = om.writeValueAsString(person);

        //2. 创建请求对象
        IndexRequest ir = new IndexRequest(index, type, person.getId().toString());
        ir.source(json, XContentType.JSON);

        //3. 创建内容
        IndexResponse index = client.index(ir, RequestOptions.DEFAULT);
        System.out.println(index.toString());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/23 17:00
     *  @Description: 删除索引
     */
    @Test
    public void del() throws IOException {
        //1. 创建删除索引请求
        DeleteIndexRequest dir = new DeleteIndexRequest();
        dir.indices(index);
        //2. 执行删除
        AcknowledgedResponse delete = client.indices().delete(dir, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/23 16:57
     *  @Description: 查询索引是否存在
     */
    @Test
    public void exists() throws IOException {
        //1. 准备 request
        GetIndexRequest gir = new GetIndexRequest();
        gir.indices(index);

        //2. 查询索引是否存在
        boolean exists = client.indices().exists(gir, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/23 16:27
     *  @Description: 创建索引
     */
    @Test
    public void start() throws IOException {
        //1. 创建索引的 Settings
        Settings settings = Settings.builder()
                // 分片数量
                .put("number_of_shards", 3)
                // 备份数量
                .put("number_of_replicas", 1)
                .build();


        //2. 创建索引的 Mappings
        /*
            "mappings": {
                "man": {
                  "properties": {
                    "name": {
                      "type": "text"
                    },
                    "age": {
                      "type": "integer"
                    }
                    "birthday": {
                      "type": "date",
                      "format": "yyy-MM-dd"
                    }
                  }
                }
              }
         */
        // startObject() 和 endObject() 必须成对出现相当于 大括号
        XContentBuilder mappings = JsonXContent.contentBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("name")
                            .field("type", "text")
                        .endObject()
                        .startObject("age")
                            .field("type", "integer")
                        .endObject()
                        .startObject("birthday")
                            .field("type", "date")
                            .field("format", "yyyy-MM-dd")
                        .endObject()
                    .endObject()
                .endObject();

        //3. 将 settings 和 mappings 封装
        CreateIndexRequest request = new CreateIndexRequest(index)
                .settings(settings)
                .mapping(type, mappings);

        //4. 通过 Client 对象去连接 ES 并执行创建
        CreateIndexResponse resp = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(resp.toString());
    }

}
