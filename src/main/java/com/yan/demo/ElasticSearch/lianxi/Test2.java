package com.yan.demo.ElasticSearch.lianxi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.demo.ElasticSearch.ESClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Test2 {
    ObjectMapper om = new ObjectMapper();
    RestHighLevelClient client = ESClient.getClient();
    String index = "sms-logs-index";
    String type = "sms-logs-type";

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/7 13:20
     *  @Description: 查询经纬度范围内的数据
     */
    @Test
    public void kkle() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        List<GeoPoint> list = new ArrayList<>();
        list.add(new GeoPoint(39.99, 116.298));
        list.add(new GeoPoint(39.97, 116.295));
        list.add(new GeoPoint(39.98, 116.297));
        ssb.query(QueryBuilders.geoPolygonQuery("location", list));

        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/7 12:31
     *  @Description: 查找某个字段的最大，最小值，平均值
     */
    @Test
    public void kke() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.aggregation(AggregationBuilders.extendedStats("agg").field("fee"));

        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        ExtendedStats agg = search.getAggregations().get("agg");
        System.out.println("最大:" +agg.getMax() + " 最小：" + agg.getMin());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/5 12:09
     *  @Description: 范围统计
     */
    @Test
    public void range() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();

        RangeAggregationBuilder agg = AggregationBuilders.range("aggse").field("fee")
                .addUnboundedTo(5)
                .addRange(5, 10)
                .addUnboundedFrom(10);

        ssb.aggregation(agg);
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        Range range = search.getAggregations().get("aggse");
        for (Range.Bucket bucket : range.getBuckets()) {
            System.out.println("from = " +bucket.getFromAsString() + "  to =" + bucket.getToAsString() +  " docCount = " + bucket.getDocCount() );
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/5 11:36
     *  @Description: 统计省市县的的数量
     */
    @Test
    public void cardSe() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.aggregation(AggregationBuilders.cardinality("agg").field("province"));

        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        Cardinality aggregations = search.getAggregations().get("agg");
        System.out.println(aggregations.getValue());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/5 11:14
     *  @Description: 高亮查询
     */
    @Test
    public void highlight() throws IOException {
        // 构建search
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        // 构建match查询
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.matchQuery("smsContent", "中国"));

        // 指定高亮字段，前缀后缀，和展示字段个字数
        HighlightBuilder hb = new HighlightBuilder();
        hb.field("smsContent", 10)
                .preTags("<font color=‘red’>")
                .postTags("</font>");

        ssb.highlighter(hb);
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getHighlightFields().get("smsContent"));
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/3 11:49
     *  @Description: filter 查询不会计算评分，比query 快一点，会有缓存
     */
    @Test
    public void filterSearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        // 增加2个filter
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        BoolQueryBuilder bqb = QueryBuilders.boolQuery();
        bqb.filter(QueryBuilders.termQuery("corpName", "腾讯课堂"));
        bqb.filter(QueryBuilders.rangeQuery("fee").gte(85));


        ssb.query(bqb);
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/3 11:32
     *  @Description: 使用 boosting 控制数据评分
     */
    @Test
    public void boosting() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();

        BoostingQueryBuilder boostingQueryBuilder = QueryBuilders.boostingQuery(
                QueryBuilders.matchQuery("smsContent", "我们"),
                QueryBuilders.matchQuery("smsContent", "事情")
        ).negativeBoost(0.2F);

        ssb.query(boostingQueryBuilder);
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getScore());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/3 10:56
     *  @Description: 多条件查询： should 满足一个即可    must 全部满足   mustNot 不等于
     */
    @Test
    public void boolQuery () throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        BoolQueryBuilder bqb = QueryBuilders.boolQuery();
        bqb.should(QueryBuilders.termQuery("province", "晋城"));
        bqb.should(QueryBuilders.termQuery("province", "上海"));
        bqb.mustNot(QueryBuilders.termQuery("operatorId", 2));
        bqb.must(QueryBuilders.matchQuery("smsContent", "我们 js").operator(Operator.OR));
        ssb.query(bqb);

        sr.source(ssb);
        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/3 10:28
     *  @Description: 先查询后删除
     */
    @Test
    public void deleteByQuerySearch() throws IOException {
        DeleteByQueryRequest dbqr = new DeleteByQueryRequest(index);
        dbqr.types(type);

        dbqr.setQuery(QueryBuilders.rangeQuery("fee").lt(4));

        BulkByScrollResponse bulkByScrollResponse = client.deleteByQuery(dbqr, RequestOptions.DEFAULT);
        System.out.println(bulkByScrollResponse.toString());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/10/3 9:57
     *  @Description: scroll 实现内存分页
     */
    @Test
    public void scrollSearch() throws IOException {
        // 1. 创建searchRequest
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        // 2. 指定scroll保存时间信息
        sr.scroll(TimeValue.timeValueMinutes(1L));

        // 3. 指定查询条件
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.size(3);
        ssb.sort("fee", SortOrder.DESC);
        ssb.query(QueryBuilders.matchAllQuery());

        sr.source(ssb);
        // 4. 获取返回结果 scroll_id ， source
        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);

        String scrollId = search.getScrollId();
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }

        while (true) {
            // 5. 循环 - 创建SearchScrollRequest
            SearchScrollRequest ssr = new SearchScrollRequest(scrollId);

            // 6. 指定scroll_id的保存时间
            ssr.scroll(TimeValue.timeValueMinutes(1L));

            // 7. 执行查询返回结果
            SearchResponse scroll = client.scroll(ssr, RequestOptions.DEFAULT);
            SearchHit[] hits = scroll.getHits().getHits();
            if (hits != null && hits.length > 0) {
                System.out.println("下一页");
                // 8. 判断是否查询到了数据，输出
                for (SearchHit documentFields : hits) {
                    System.out.println(documentFields);
                }
            } else {
                // 9. 判断没有查询到数据退出循环
                System.out.println("最后一页");
                break;
            }

        }

        // 10. 删除scroll 创建clearScrollRequest
        ClearScrollRequest csr = new ClearScrollRequest();
        csr.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(csr, RequestOptions.DEFAULT);
        System.out.println(clearScrollResponse.isSucceeded());
    }


    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 11:40
     *  @Description: 正则查询
     */
    @Test
    public void regexpSearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.regexpQuery("mobile", "138[0-9]{8}"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 11:18
     *  @Description: 范围查询
     */
    @Test
    public void rangeSearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        // fee 要查询的字段，在 50 - 60 范围内的
        ssb.query(QueryBuilders.rangeQuery("fee").gte(50).lte(60));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 11:09
     *  @Description: 模糊搜索，和mysql 的like一样 这里用 * 号 和占位符 ？
     */
    @Test
    public void wildcardSearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.wildcardQuery("corpName", "腾讯*"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 10:36
     *  @Description: 根据关键字进行模糊搜索
     */
    @Test
    public void fuzzySearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.fuzzyQuery("corpName", "阿里纷纷").prefixLength(2));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 10:13
     *  @Description: 前缀查询
     */
    @Test
    public void prefixSearch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.prefixQuery("corpName", "阿里"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }


    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 10:04
     *  @Description: 通过多个ID查询数据
     */
    @Test
    public void idsSearch () throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.idsQuery().addIds("1", "2"));

        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/29 9:13
     *  @Description: 通过ID 查询数据
     */
    @Test
    public void idSearch() throws IOException {
        GetRequest gr = new GetRequest(index, type, "1");
        GetResponse resp = client.get(gr, RequestOptions.DEFAULT);
        System.out.println(resp.getSourceAsString());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/26 12:05
     *  @Description: multi_match 一个关键在在多个字段查询结果
     */
    @Test
    public void multiMatch() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.multiMatchQuery("北京", "province", "corpName"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 18:02
     *  @Description: 指定一个分词的字段，是否同时找到 两个关键字
     */
    @Test
    public void boolMatchQuery() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(QueryBuilders.matchQuery("smsContent", "祖国 看到").operator(Operator.AND));
        ssb.size(20);
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 17:36
     *  @Description: 查询索引下所有数据
     */
    @Test
    public void matchAllQuery() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        // 不指定 page,size 默认只查询 10 条数据
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.from(0);
        ssb.size(20);
        ssb.query(QueryBuilders.matchAllQuery());
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 17:31
     *  @Description: match 查询 会根据你查询的字段，动态判断是否需要分词。或转换数据类型
     */
    @Test
    public void matchQuery() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.from(0);
        ssb.size(20);
        ssb.query(QueryBuilders.matchQuery("ipAddr", "127.0.0.14"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 17:10
     *  @Description: terms 查询相当于 mysql 的 name IN(....)
     */
    @Test
    public void termsQuery() throws IOException {
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.from(0);
        ssb.size(20);
        SearchSourceBuilder query = ssb.query(QueryBuilders.termsQuery("province", "北京", "上海"));
        sr.source(query);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsMap().toString());
        }
    }


    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 16:51
     *  @Description: term查询，相当于mysql 的 name = "张三"
     */
    @Test
    public void termQuery() throws IOException {
        // 创建search 对象
        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        // 指定查询条件
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.from(0);
        ssb.size(10);
        ssb.query(QueryBuilders.termQuery("province", "北京"));
        sr.source(ssb);

        // 执行查询
        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        SearchHit[] hits = search.getHits().getHits();

        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsMap().toString());
        }
    }
    @Test
    public void termQuery2() throws IOException {

        SearchRequest sr = new SearchRequest(index);
        sr.types(type);

        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.from(0);
        ssb.size(10);
        ssb.query(QueryBuilders.termQuery("corpName", "格力汽车"));
        sr.source(ssb);

        SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsMap().toString());
        }
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 14:11
     *  @Description: 批量添加测试数据
     */
    @Test
    public void insertData() throws IOException, InterruptedException {
        String longcode = "1008687";
        String mobile ="138340658";
        List<String> companies = new ArrayList<>();
        companies.add("腾讯课堂");
        companies.add("阿里旺旺");
        companies.add("海尔电器");
        companies.add("海尔智家公司");
        companies.add("格力汽车");
        companies.add("苏宁易购");
        List<String> provinces = new ArrayList<>();
        provinces.add("北京");
        provinces.add("重庆");
        provinces.add("上海");
        provinces.add("晋城");
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 1; i <16 ; i++) {
            Thread.sleep(100);
            SmsLogs s1 = new SmsLogs();
            s1.setId(String.valueOf(i));
            s1.setCreateDate(new Date());
            s1.setSendDate(new Date());
            s1.setLongCode(longcode+i);
            s1.setMobile(mobile+2*i);
            s1.setCorpName(companies.get(i%5));
            s1.setSmsContent(SmsLogs.doc.substring((i-1)*10,i*10));
            s1.setState(i%2);
            s1.setOperatorId(i%3);
            s1.setProvince(provinces.get(i%4));
            s1.setIpAddr("127.0.0."+i);
            s1.setReplyTotal(i*3);
            s1.setFee(i*6);
            String json1  = om.writeValueAsString(s1);
            bulkRequest.add(new IndexRequest(index, type, s1.getId()).source(json1, XContentType.JSON));
            System.out.println("数据"+i+s1.toString());
        }

        BulkResponse responses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(responses.status().getStatus());
    }

    /**
     *  @author: Do (Yan)
     *  @Date: 2020/9/24 13:55
     *  @Description: 创建索引和type
     */
    @Test
    public void createIndex() throws IOException {

        Settings settings = Settings.builder()
                .put("number_of_shards", 3)
                .put("number_of_replicas", 1)
                .build();

        XContentBuilder xb = JsonXContent.contentBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("createDate")
                            .field("type", "date")
                            .field("format", "yyyy-MM-dd")
                        .endObject()
                        .startObject("sendDate")
                            .field("type", "date")
                            .field("format", "yyyy-MM-dd")
                        .endObject()
                        .startObject("longCode")
                            .field("type", "keyword")
                        .endObject()
                        .startObject("mobile")
                            .field("type", "keyword")
                        .endObject()
                        .startObject("corpName")
                            .field("type", "keyword")
                        .endObject()
                        .startObject("smsContent")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                        .endObject()
                        .startObject("state")
                            .field("type", "integer")
                        .endObject()
                        .startObject("operatorId")
                            .field("type", "integer")
                        .endObject()
                        .startObject("province")
                            .field("type", "keyword")
                        .endObject()
                        .startObject("ipAddr")
                            .field("type", "ip")
                        .endObject()
                        .startObject("replyTotal")
                            .field("type", "integer")
                        .endObject()
                        .startObject("fee")
                            .field("type", "long")
                        .endObject()
                    .endObject()
                .endObject();


        CreateIndexRequest cir = new CreateIndexRequest(index);
        cir.settings(settings);
        cir.mapping(type, xb);

        CreateIndexResponse resp = client.indices().create(cir, RequestOptions.DEFAULT);
        System.out.println(resp);
    }

}
