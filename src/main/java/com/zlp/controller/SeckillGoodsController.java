package com.zlp.controller;

import com.zlp.common.Result;
import com.zlp.pojo.TbSeckillGoods;
import com.zlp.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName: SeckillGoodsController
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/18 21:49
 * @Version 1.0
 **/
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

    @Autowired
    SeckillGoodsService sgs;

    @RequestMapping("findAll")
    public List<TbSeckillGoods> finaAll(){
        return sgs.finaAll();
    }

    @RequestMapping("/findOne/{id}")
    public TbSeckillGoods findOne(@PathVariable("id") long id){
        return sgs.findOne(id);
    }

    @RequestMapping("/saveOrder/{id}")
    public Result saveOrder(@PathVariable("id") Long id){
        String userId = "zlp";
        return sgs.saveOrder(id,userId);
    }
}
