package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;

import java.util.List;

public interface AttrInfoService {

    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);
}
