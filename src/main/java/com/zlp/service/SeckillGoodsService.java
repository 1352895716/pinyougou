package com.zlp.service;

import com.zlp.common.Result;
import com.zlp.pojo.TbSeckillGoods;

import java.util.List;

/**
 * @ClassName: SeckillGoodsService
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/18 21:53
 * @Version 1.0
 **/
public interface SeckillGoodsService {

    public List<TbSeckillGoods> finaAll();
    public TbSeckillGoods findOne(long id);
    public Result saveOrder(Long id,String userId);
}
