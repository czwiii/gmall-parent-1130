package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.List;

public interface SearchService {
    List<JSONObject> getBaseCategory2Index();

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    void createIndex(String index, String type);

    SearchResponseVo searchList(SearchParam searchParam);

    void hotScore(Long skuId);

}
