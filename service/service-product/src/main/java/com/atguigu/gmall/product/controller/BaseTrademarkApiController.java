package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.util.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class BaseTrademarkApiController {

    @Resource
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Long page,
                                @PathVariable("limit") Long limit){

        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.baseTrademark(page,limit);
        return Result.ok(baseTrademarkIPage);
    }

    @GetMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList(){

        List<BaseTrademark> baseTrademarkList =  baseTrademarkService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }
}
