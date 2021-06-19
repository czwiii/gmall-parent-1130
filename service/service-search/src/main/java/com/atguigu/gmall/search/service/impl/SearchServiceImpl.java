package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.service.SearchService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregator;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private GoodsRepository goodsRepository;

    @Resource
    private RedisTemplate redisTemplate;

    @Override // 更新ES热度值
    public void hotScore(Long skuId) {
        //先缓存
        // 每次访问都在redis相同的key中+1，increment：增量
        Long increment = redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1);

        //更新ES
        // 每当访问次数到达10次时更新一次ES热度值，optional：可选择的
        if (increment%10==0){
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(increment); // 更新热度值
            goodsRepository.save(goods);
        }
    }

    @Override // 查询所有分类
    public List<JSONObject> getBaseCategory2Index() {

        List<JSONObject> jsonObjects = new ArrayList<>();

        // 从数据层（product）查出所有分类数据
        // 此时获取的时三个分类的联表，所以会有多个相同一级分类和二级分类
        List<BaseCategoryView> baseCategoryViewList =  productFeignClient.getCateGoryView();

        // 对分类数据集合进行分组处理
        // 通过将集合转换为这么一种叫做 “流” 的元素序列，通过声明性方式，能够对集合中的每个元素进行一系列并行或串行的流水线操作
        Stream<BaseCategoryView> stream = baseCategoryViewList.stream(); // 函数式编程
        // 流通过collect()可以根据Collects转换为其它类型，groupingBy()根据Id对list进行分组
        // 双冒号是java8为了化简Lambda表达式的一种写法
        Map<Long, List<BaseCategoryView>> c1Map = stream.collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        /**
         * Map.entrySet() 这个方法返回的是一个Set<Map.Entry<K,V>>，Map.Entry 是Map中的一个接口
         * 他的用途是表示一个映射项（里面有Key和Value），而Set<Map.Entry<K,V>>表示一个映射项的Set
         * Map.Entry里有相应的getKey和getValue方法，即JavaBean，让我们能够从一个项中取出Key和Value
         */
        // 简单来说，set就是一个存放多个Map.Entry<K,V>键值对的集合，这个Entry可以getKey和getValue方法进行取值
        Set<Map.Entry<Long, List<BaseCategoryView>>> c1EntrySet = c1Map.entrySet();
        for (Map.Entry<Long, List<BaseCategoryView>> c1Object : c1EntrySet) {
            JSONObject c1JSON = new JSONObject();
            Long c1Id = c1Object.getKey(); // 因为以id进行分组，所以key就是id
            // 因为数据库中，同个id下其它字段的值也都是一样的，所以获取集合的第一个元素即可
            String c1Name = c1Object.getValue().get(0).getCategory1Name();
            c1JSON.put("categoryId",c1Id);
            c1JSON.put("categoryName",c1Name);

            // 二级分类
            List<JSONObject> c2jsonObjects = new ArrayList<>(); // 一定要把二级分类集合放到一级分类的for循环中
            Map<Long, List<BaseCategoryView>> c2Map = c1Object.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> c2Object : c2Map.entrySet()) {
                JSONObject c2JSON = new JSONObject();
                Long c2Id = c2Object.getKey();
                String c2Name = c2Object.getValue().get(0).getCategory2Name(); // 注意要换成二级分类名称
                c2JSON.put("categoryId",c2Id);
                c2JSON.put("categoryName",c2Name);

                //三级分类
                List<JSONObject> c3jsonObjects = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> c3Map = c2Object.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> c3Object : c3Map.entrySet()) {
                    JSONObject c3JSON = new JSONObject();
                    Long c3Id = c3Object.getKey();
                    String c3Name = c3Object.getValue().get(0).getCategory3Name();
                    c3JSON.put("categoryId", c3Id);
                    c3JSON.put("categoryName", c3Name);
                    c3jsonObjects.add(c3JSON); // 将三级分类信息放入集合
                }
                c2JSON.put("categoryChild",c3jsonObjects); // 将三级分类的集合放入二级分类信息
                c2jsonObjects.add(c2JSON); // 将二级分类信息放入集合
            }
            c1JSON.put("categoryChild",c2jsonObjects); // 将二级分类的集合放入一级分类信息
            jsonObjects.add(c1JSON);
        }
        return jsonObjects;
    }

    @Override // 根据上架商品的skuId，将数据添加到es中
    public void onSale(Long skuId) {
        //skuInfo详情,attr平台属性,tm品牌,catagory分类
        Goods goods = productFeignClient.getGoodsBySkuId(skuId);
        if (goods!=null){
            goods.setCreateTime(new Date());
            goodsRepository.save(goods);
        }
    }

    @Override // 根据下架商品的skuId，将数据从es中删除
    public void cancelSale(Long skuId) {
        Goods goods = new Goods();
        goods.setId(skuId);
        goodsRepository.delete(goods);
    }

    @Override // 创建ES索引
    public void createIndex(String index, String type) {
        Class<?> indexClass = null;
        String className = "com.atguigu.gmall.model.list."; // 全类名前缀
        // 根据index请求的类名动态生成映射类
        try {
            indexClass = Class.forName(className + index);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        elasticsearchRestTemplate.createIndex(indexClass);
        elasticsearchRestTemplate.putMapping(indexClass);
    }

    @Override //根据页面参数查询商品列表，并对其进行聚合
    public SearchResponseVo searchList(SearchParam searchParam) {

        // 建立查询语句，根据页面传输的请求对象，拼接成DSL查询语句
        SearchRequest searchRequest = getSearchRequest(searchParam);

        // 执行查询语句
        SearchResponse searchResponse = null; // 总查询结果
        try {
            // 根据查询条件查询索引数据
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析封装返回结果，对查询的结果进行聚合，即分组
        SearchResponseVo searchResponseVo = getSearchResponseVo(searchResponse);
        return searchResponseVo;
    }

    private SearchRequest getSearchRequest(SearchParam searchParam){

        // 参数对象中的搜索参数不能同时为空，否则前端应该返回查询失败
//        Long category1Id = searchParam.getCategory1Id();
//        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id(); // 分类id
        String keyword = searchParam.getKeyword(); // 关键字
        String[] props = searchParam.getProps(); // 页面请求的属性数组
        String trademark = searchParam.getTrademark(); // 品牌名称
        String order = searchParam.getOrder(); // 排序规则

        // 开始拼接DSL查询语句，query是过滤条件，是为了获取Goods商品列表，不需要解析！
        SearchRequest searchRequest = new SearchRequest(); // 查询条件
        searchRequest.indices("goods"); // 搜索索引范围
        searchRequest.types("info"); // 搜索表范围

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0); // 分页索引
        searchSourceBuilder.size(60); // 分页条数

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder(); // query下的复合搜索的DSL封装
        if (!StringUtils.isEmpty(trademark)){
            //封装品牌
            String[] tmSplit = trademark.split(":"); // 格式：trademark=2:华为  tmId:tmName
            String tmId = tmSplit[0];
            String tmName = tmSplit[1];

            TermQueryBuilder tmIdTermsQueryBuilder = new TermQueryBuilder("tmId",tmId);
            boolQueryBuilder.filter(tmIdTermsQueryBuilder);
            TermQueryBuilder tmNameTermsQueryBuilder = new TermQueryBuilder("tmName",tmName);
            boolQueryBuilder.filter(tmNameTermsQueryBuilder);
        }
        if (!StringUtils.isEmpty(category3Id)){
            //封装分类
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id",category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (!StringUtils.isEmpty(keyword)){
            //封装关键字，match会进行分词处理，会生成多个结果最后取并集
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        if (props!=null&&props.length>0){
            //封装平台属性过滤条件
            for (String prop : props) {
                // 由于前端请求的每个元素格式是 props=23:4G:运行内存，所以要对每个元素根据":"进行拆分同时生成过滤条件
                String[] attrSplit = prop.split(":");
                String attrId = attrSplit[0];
                String attrValue = attrSplit[1]; // 一定要注意前端传过来的格式，是Value在前Name在后
                String attrName = attrSplit[2];

                //第三层,nested下也有一层bool
                BoolQueryBuilder attrNestedBoolQueryBuilder = new BoolQueryBuilder();

                //第四层
                // 此时要用精准过滤term，要区分terms和term，term是terms的内部结构，但此时已经nested代替了terms
                // 要加attrs可能是因为要对应映射类关系，attrId是映射类attrs的一个子属性
                TermQueryBuilder attrIdTerm = new TermQueryBuilder("attrs.attrId", attrId);
                attrNestedBoolQueryBuilder.filter(attrIdTerm);
                TermQueryBuilder attrNameTerm = new TermQueryBuilder("attrs.attrName", attrName);
                attrNestedBoolQueryBuilder.filter(attrNameTerm);
                TermQueryBuilder attrValueTerm = new TermQueryBuilder("attrs.attrValue", attrValue);
                attrNestedBoolQueryBuilder.filter(attrValueTerm);

                //第二层，nested结构，相当于terms
                // 因为属性是nested类型，所以记得要创建一个NestedQueryBuilder，否则会出现什么也查不出来
                NestedQueryBuilder attrNestedQueryBuilder = new NestedQueryBuilder("attrs",attrNestedBoolQueryBuilder, ScoreMode.None);
                //第一层
                boolQueryBuilder.filter(attrNestedQueryBuilder);
                //总结：boolQuery -> NestedQuery -> NestedBoolQuery -> TermsQuery
                //bool要用filter，term和nested可以new的时候可以通过参数建立语句
            }
        }
        searchSourceBuilder.query(boolQueryBuilder); //顶层，query是最外的一层

        //建立DSL聚合语句，并根据聚合分组进行解析，agg与query同级，互不干涉
        //商标聚合，terms是分组别名，field是根据分组的映射类属性
        // 另一组聚合函数必须是当前聚合函数分组的子分组，按逻辑是先按什么分组再按什么分组，不可能有两个字段同时分组
        // terms是什么都不处理，即不count，add，也不比较大小值，只是把最原始的数据放到bucket中
        TermsAggregationBuilder tmTtermsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(tmTtermsAggregationBuilder);
        //老师代码
//        searchSourceBuilder.aggregation(tmTtermsAggregationBuilder
//            .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
//            .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //属性聚合
        // 此时不用terms了，因为属性字段是nested内嵌结构，第二个参数path同field，只是nested的独有写法
        // attrs字段不同于普通字段内容直接就是数据，attrs类似于引用类型的字段，有自己的内部结构
        // 此时数据有三层结构，attrs是结构字段，attrId是主聚合，attrValue和attrName都是attrId的子聚合
        // attrId和属性名称attrName是一对一，attrId和属性值attrValue是一对多
        // 所以推测键值对关系不是attrName:attrValue，而是 attrId:attrName 和 attrId:[attrValue]
        NestedAggregationBuilder attrsNestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")));
        searchSourceBuilder.aggregation(attrsNestedAggregationBuilder);
        //老师代码
//        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg", "attrs")
//                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
//                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
//                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))));

        //按照热度和评分算法排序，排序和查询、聚合同级
            // 后台拼接：1:hotScore 2:price  前台页面传递：order=2:desc 1：综合排序/热点  2：价格
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            String orderType = split[0]; // 1 热度/ 2 价格
            String orderSort = split[1]; // asc/desc
            if (orderType.equals("1")){
                orderType = "hotScore";
            }else if (orderType.equals("2")){
                orderType = "price";
            }
            // 参数：排序的字段，排序的顺序
            searchSourceBuilder.sort(orderType, orderSort.equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }

        System.out.println("DSL语句：\t" + searchSourceBuilder.toString()); // 打印DSL语句
        searchRequest.source(searchSourceBuilder); // 搜索语句，参数相当于{}，且不能为null
        return searchRequest;
    }

    private SearchResponseVo getSearchResponseVo(SearchResponse searchResponse){

        //这部分不是解析！只是将搜索结果放到商品列表中
        SearchResponseVo searchResponseVo = new SearchResponseVo(); // 页面数据对象
        if (searchResponse!=null&&searchResponse.getHits().totalHits > 0){
            List<Goods> goodsList = new ArrayList<>(); // 封装数据的集合
            SearchHit[] hits = searchResponse.getHits().getHits(); // 有两层hits
            for (SearchHit hit : hits) {
                String source = hit.getSourceAsString(); // 获取sku数据
                // 将字符串转换为JSON再映射为对象
                Goods goods = JSONObject.parseObject(source, Goods.class);
                goodsList.add(goods);
            }
            searchResponseVo.setGoodsList(goodsList); // 放入搜索结果的商品列表
        }

        //从这开始才对聚合分组进行解析，根据搜索结果，提取出分组信息
        // 取出搜索的聚合结果，聚合函数是结果解析并非过滤条件，所以是SearchResponse开始执行
        Aggregations aggregations = searchResponse.getAggregations();

        //非流式编程循环
//        // 默认的返回结果Aggregation是一个接口，要根据返回类型转换为需要的子接口类型ParsedLongTerms、ParsedStringTerms等等
//        // 获取聚合函数的数据有三层，Aggregations.Aggregation.Bucket
//        ParsedLongTerms tmIdAgg = aggregations.get("tmIdAgg");
//        List<? extends Terms.Bucket> tmBuckets = tmIdAgg.getBuckets();
//        if (tmBuckets!=null&&tmBuckets.size()>0){
//            ArrayList<SearchResponseTmVo> tmVOList = new ArrayList<>();
//            for (Terms.Bucket tmBucket : tmBuckets) {
//                //商品id
//                // 获取keyId并转换为long类型
//                Long tmId = tmBucket.getKeyAsNumber().longValue();
//                //商品名称
//                // 注意加Agg，这里调用的是聚合函数中自定义的别名字段
//                // get(0)即可，同个品牌的分组下，品牌信息都是一样的
//                ParsedStringTerms tmNameAgg = tmBucket.getAggregations().get("tmNameAgg");
//                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
//                //商标logo
//                ParsedStringTerms tmLogoUrlAgg = tmBucket.getAggregations().get("tmLogoUrlAgg");
//                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
//                SearchResponseTmVo tmVo = new SearchResponseTmVo(tmId,tmName,tmLogoUrl);
//                tmVOList.add(tmVo);
//            }
//            searchResponseVo.setTrademarkList(tmVOList); // 放入商标列表
//        }

        //使用流式编程
        if (aggregations!=null){

            //商标聚合解析
            ParsedLongTerms tmIdAgg = aggregations.get("tmIdAgg");
            List<SearchResponseTmVo> tmVOList = tmIdAgg.getBuckets().stream().map(tmBucket -> {
                //商品id
                Long tmId = tmBucket.getKeyAsNumber().longValue();
                //商品名称
                // 注意加Agg，这里调用的是聚合函数中自定义的别名字段
                ParsedStringTerms tmNameAgg = tmBucket.getAggregations().get("tmNameAgg");
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                //商标logo
                ParsedStringTerms tmLogoUrlAgg = tmBucket.getAggregations().get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();

                SearchResponseTmVo tmVo = new SearchResponseTmVo(tmId, tmName, tmLogoUrl);
                return tmVo;
            }).collect(Collectors.toList());
            searchResponseVo.setTrademarkList(tmVOList); // 放入商标列表

            //属性聚合解析
            ParsedNested attrAgg = aggregations.get("attrsAgg");
            ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
            List<SearchResponseAttrVo> attrVoList = attrIdAgg.getBuckets().stream().map(attrIdBucket -> {
                //属性id
                Long attrId = attrIdBucket.getKeyAsNumber().longValue();
                //属性名称
                ParsedStringTerms attrNameAgg = attrIdBucket.getAggregations().get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                //属性值
                ParsedStringTerms attrValueAgg = attrIdBucket.getAggregations().get("attrValueAgg");
                // 此时就不能跟其它字段一样get(0)了，属性值是一对多的关系，所以每个sku的属性属性值是一个集合
                List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                    String attrValue = attrValueBucket.getKeyAsString();
                    return attrValue;
                }).collect(Collectors.toList());

                SearchResponseAttrVo attrVo = new SearchResponseAttrVo(attrId,attrName,attrValueList);
                return attrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(attrVoList); // 放入属性筛选列表
        }
        return searchResponseVo;
    }
}
