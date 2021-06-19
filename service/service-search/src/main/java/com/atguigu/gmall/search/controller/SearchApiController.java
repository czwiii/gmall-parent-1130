package com.atguigu.gmall.search.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.util.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("api/search")
public class SearchApiController {

    @Resource
    private SearchService searchService;

    @GetMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){
        searchService.hotScore(skuId);
    }

    @PostMapping("searchList")
    SearchResponseVo searchList(@RequestBody SearchParam searchParam){
        SearchResponseVo searchResponseVo = searchService.searchList(searchParam);
        return searchResponseVo;
    }

    @GetMapping("createIndex/{index}/{type}")
    Result createIndex(@PathVariable("index") String index,
                       @PathVariable("type") String Type){
        searchService.createIndex(index,Type);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId){
        searchService.cancelSale(skuId);
    }

    @GetMapping("onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId){
        searchService.onSale(skuId);
    }

    @GetMapping("/getBaseCategory2Index")
    List<JSONObject> getBaseCategory2Index(){
        List<JSONObject> jsonObjectList = searchService.getBaseCategory2Index();
        return jsonObjectList;
    }
}
