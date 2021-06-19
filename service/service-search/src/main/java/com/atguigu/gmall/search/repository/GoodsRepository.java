package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// 泛型T：索引名称，ID：id数据类型
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
