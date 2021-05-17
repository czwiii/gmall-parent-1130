package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.util.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.CategoryService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo attrInfo){

        String id = categoryService.saveAttrInfo(attrInfo);
        return Result.ok();
    }

    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId){

        List<BaseAttrValue> baseAttrValueList = categoryService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }

    @GetMapping("getCategory1")
    public Result getCategory1(){

        List<BaseCategory1> baseCategory1List = categoryService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){

        List<BaseCategory2> baseCategory2List = categoryService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){

        List<BaseCategory3> baseCategory3List = categoryService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }
}
