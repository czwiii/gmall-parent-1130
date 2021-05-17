package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.aspectj.weaver.ast.Var;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {

    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public IPage<BaseTrademark> baseTrademark(Long page, Long limit) {

        Page<BaseTrademark> baseTrademarkPage = new Page<>();
        baseTrademarkPage.setPages(page);
        baseTrademarkPage.setSize(limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkMapper.selectPage(baseTrademarkPage, null);
        return baseTrademarkIPage;
    }
}
