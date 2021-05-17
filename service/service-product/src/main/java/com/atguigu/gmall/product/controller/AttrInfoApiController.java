package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.util.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.service.AttrInfoService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class AttrInfoApiController {

    @Resource
    private AttrInfoService attrInfoService;

    @RequestMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable("category1Id") Long category1Id,
                               @PathVariable("category2Id") Long category2Id,
                               @PathVariable("category3Id") Long category3Id){

        List<BaseAttrInfo> attrInfoList = attrInfoService.attrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(attrInfoList);
    }
}
