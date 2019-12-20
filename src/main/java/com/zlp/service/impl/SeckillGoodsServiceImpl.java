package com.zlp.service.impl;

import com.zlp.common.IdWorker;
import com.zlp.common.OrderRecord;
import com.zlp.common.Result;
import com.zlp.common.SystemConst;
import com.zlp.mapper.TbSeckillGoodsMapper;
import com.zlp.pojo.TbSeckillGoods;
import com.zlp.pojo.TbSeckillOrder;
import com.zlp.service.SeckillGoodsService;
import com.zlp.thread.OrderCreateThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @ClassName: SeckillGoodsService
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/18 21:56
 * @Version 1.0
 **/
@Service
@Transactional
public class SeckillGoodsServiceImpl implements SeckillGoodsService{

    @Autowired
    TbSeckillGoodsMapper tbSeckillGoodsMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public List<TbSeckillGoods> finaAll() {
        //获取全部的值
        return redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).values();
    }

    @Override
    public TbSeckillGoods findOne(long id) {
        return (TbSeckillGoods) redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).get(id);
    }

    @Resource
    IdWorker idWorker;
    /*
     * @Autor: 13528
     * 1. 1,2,3步在多线程时会发生线程安全问题,
     * 解决这个问题可以使用redis中的队列
     * 2. 4,5,6,7,8步和return可以异步执行，这样可以提高并发效率
     * 使用spring封装的线程池来解决
     */
   /* @Override
    public Result saveOrder(long id, String userId) {
        //
        //1.从redis获取秒杀商品
        TbSeckillGoods good = (TbSeckillGoods) redisTemplate
                .boundHashOps(TbSeckillGoods.class.getSimpleName()).get(id);
        //2.判断商品是否存在，或库存是否《=0
        if(null == good || good.getStockCount() <= 0){
            //3.商品不存在，或库存《=0，返回失败，提示已售馨
            return new Result(false,"商品已售馨！");
        }


        //4.生成秒杀订单，将订单保存到redis缓存
        long orderId = idWorker.nextId();
        TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
        tbSeckillOrder.setId(orderId);
        tbSeckillOrder.setSellerId(good.getSellerId());
        tbSeckillOrder.setUserId(userId);
        tbSeckillOrder.setMoney(good.getCostPrice());
        tbSeckillOrder.setCreateTime(new Date());
        tbSeckillOrder.setStatus("0");//0 --  未支付

        redisTemplate.boundHashOps(TbSeckillOrder.class.getSimpleName()).put(userId,tbSeckillOrder);
        //5.秒杀商品库存量减一
        good.setStockCount(good.getStockCount()-1);
        //6.判断库存量是否《=0
        if(good.getStockCount()<=0){
            //7.是，将秒杀商品更新到数据库，删除秒杀商品缓存
            good.setStatus("0");
            tbSeckillGoodsMapper.updateByPrimaryKey(good);
            //删除缓存不会
            redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).delete(id);
        }else {
            //8.否，将秒杀商品更新到缓存，返回成功！
            redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).put(id,good);
        }
        return new Result(true,"抢购成功！");
    }*/
   /*
    * @Autor: 13528
    * 解决1,2,3步发生的超卖问题
    * 可以使用同步代码块来解决，但是加锁会影响效率，且在分布式条件下还是会发生同样的问题
    * 这时可以使用分布式锁来解决，也可使用redis队列来解决
    * 下面就是使用队列来解决
    **/

   @Resource
    Executor executor;
   @Resource
    OrderCreateThread orderCreateThread;
   /*
    * @Autor: 13528
    * @Description: TODO
    * 商品hash
    * 商品queue
    * 记录queue
    * 用户set
    * 订单hash
    **/
   @Override
   public Result saveOrder(Long id, String userId) {

       //0.先判断该用户是否存在用户集合中
       Boolean member = redisTemplate.boundSetOps(SystemConst.CONST_USER_ID_PREFIX + id).isMember(userId);
       if(member){
           //正在排队，或者还未支付
           return new Result(false,"对不起，您正在排队，等待支付！！");
       }

       //---------------------------------------------------------------------------------------------------------
       //1.先从队列中获取秒杀商品的id
           id = (Long) redisTemplate.boundListOps(SystemConst.CONST_SECKILLGOODS_ID_PREFIX+id).rightPop();
           //2.判断商品是否存在
           if(null == id){
               //3.商品不存在，或库存《=0，返回失败，提示已售馨
               return new Result(false,"商品已售馨！");
       }
       //----------------------------------------------------------------------------------------------------------
       //满足
       //4.将用户放入用户集合::记录用户
       redisTemplate.boundSetOps(SystemConst.CONST_USER_ID_PREFIX + id).add(userId);
       //5.创建OrderRecord对象记录用户下单信息：用户id，商品id,放到OrderRecord队列中
       OrderRecord orderRecord = new OrderRecord(id,userId);
       redisTemplate.boundListOps(OrderRecord.class.getSimpleName()).leftPush(orderRecord);

       //6.通过线程池启动线程处理OrderRecord中的数据，返回成功！
       executor.execute(orderCreateThread);
       return new Result(true,"抢购成功！");
   }
}
