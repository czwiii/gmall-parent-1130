package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Resource
    private SpuInfoMapper spuInfoMapper;

    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public IPage<SpuInfo> selectPage(long page, long limit, Long category3Id) {

        Page<SpuInfo> infoPage = new Page<>();
        infoPage.setPages(page).setSize(limit);
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(infoPage, wrapper);
        return spuInfoIPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {

        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }
}
