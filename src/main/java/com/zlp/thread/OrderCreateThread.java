package com.zlp.thread;

import com.zlp.common.IdWorker;
import com.zlp.common.OrderRecord;
import com.zlp.mapper.TbSeckillGoodsMapper;
import com.zlp.pojo.TbSeckillGoods;
import com.zlp.pojo.TbSeckillOrder;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName: OrderCreateThread
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/19 16:04
 * @Version 1.0
 **/
@Component  //需要在service中创建对象
public class OrderCreateThread implements Runnable{

    @Resource
    RedisTemplate redisTemplate;
    @Resource
    IdWorker idWorker;

    @Resource
    TbSeckillGoodsMapper tbSeckillGoodsMapper;

    /*
      * @Autor: 13528
      * @Description:
      * 下面解决并发问题
      * 使用多线程来提高并发效率
      */
    @Override
    public void run() {
        OrderRecord orderRecord = (OrderRecord) redisTemplate.boundListOps(OrderRecord.class.getSimpleName()).rightPop();

        //3.如果不为空，在从redis中拿数据
        TbSeckillGoods good = (TbSeckillGoods) redisTemplate
                .boundHashOps(TbSeckillGoods.class.getSimpleName()).get(orderRecord.getId());


        //4.生成秒杀订单，将订单保存到redis缓存
        TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
        tbSeckillOrder.setId(idWorker.nextId());
        tbSeckillOrder.setSellerId(good.getSellerId());
        tbSeckillOrder.setUserId(orderRecord.getUserId());
        tbSeckillOrder.setMoney(good.getCostPrice());
        tbSeckillOrder.setCreateTime(new Date());
        tbSeckillOrder.setStatus("0");//0 --  未支付

        redisTemplate.boundHashOps(TbSeckillOrder.class.getSimpleName()).put(orderRecord.getUserId(),tbSeckillOrder);//

        synchronized (OrderCreateThread.class){
            good = (TbSeckillGoods) redisTemplate
                    .boundHashOps(TbSeckillGoods.class.getSimpleName()).get(orderRecord.getId());
            //5.秒杀商品库存量减一
            good.setStockCount(good.getStockCount()-1);
            //6.判断库存量是否《=0
            if(good.getStockCount()<=0){
                //7.是，将秒杀商品更新到数据库，删除秒杀商品缓存
                good.setStatus("0");
                tbSeckillGoodsMapper.updateByPrimaryKey(good);
                redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).delete(orderRecord.getId());
            }else {
                //8.否，将秒杀商品更新到缓存，返回成功！
                redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).put(orderRecord.getId(),good);
            }
        }
    }
}
