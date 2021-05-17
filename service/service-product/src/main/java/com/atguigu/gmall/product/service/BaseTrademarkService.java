package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface BaseTrademarkService {
    IPage<BaseTrademark> baseTrademark(Long page, Long limit);

    List<BaseTrademark> getTrademarkList();
}
