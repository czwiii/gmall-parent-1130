package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.util.Result;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class SpuApiController {

    @Resource
    private SpuService spuService;

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        Long SpuId = spuService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable("page") long page,
                                 @PathVariable("limit") long limit,
                                 Long category3Id){

        IPage<SpuInfo> spuInfoPage = spuService.selectPage(page,limit,category3Id);
        return Result.ok(spuInfoPage);
    }

    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){

        List<BaseSaleAttr> baseSaleAttrList = spuService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }
}
