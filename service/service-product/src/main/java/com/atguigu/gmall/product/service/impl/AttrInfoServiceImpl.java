package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.AttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.management.Query;
import java.util.List;

@Service
public class AttrInfoServiceImpl implements AttrInfoService {

    @Resource
    private BaseAttrInfoMapper baseattrInfoMapper;

    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        QueryWrapper<BaseAttrInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",category3Id);
        wrapper.eq("category_level",3);
        List<BaseAttrInfo> attrInfoList = baseattrInfoMapper.selectList(wrapper);
        for (BaseAttrInfo baseAttrInfo : attrInfoList) {
            QueryWrapper<BaseAttrValue> valueWrapper = new QueryWrapper<>();
            wrapper.eq("attr_id",baseAttrInfo.getId());
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(valueWrapper);
            baseAttrInfo.setAttrValueList(baseAttrValues);
        }
        return attrInfoList;
    }
}
