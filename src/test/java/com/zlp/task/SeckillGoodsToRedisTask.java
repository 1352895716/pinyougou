package com.zlp.task;

import com.zlp.common.SystemConst;
import com.zlp.mapper.TbSeckillGoodsMapper;
import com.zlp.pojo.TbSeckillGoods;
import com.zlp.pojo.TbSeckillGoodsExample;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: SeckillGoodsToRedisTask
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/18 17:09
 * @Version 1.0
 **/
@Component
public class SeckillGoodsToRedisTask {

    @Resource
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/15 * * * * ?")
    public void importToRedis(){
        //1.查询合法的秒杀数据：（status=1）（stackCount>0）,秒杀开始时间《=当前时间《秒杀结束时间
        //2.将数据存入redis
        System.out.println("定时任务执行了:"+new Date());

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        Date date = new Date();
        criteria.andStatusEqualTo("1")
                .andStockCountGreaterThan(0)
                .andStartTimeLessThanOrEqualTo(date)
                .andEndTimeGreaterThan(date);
        System.out.println("---------------------------");
        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
        System.out.println(tbSeckillGoods.size());
        for (TbSeckillGoods good:tbSeckillGoods){
            System.out.println(good);
            redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).put(good.getId(),good);
            //为每一个商品创建一个队列，队列中放和库存量形同数据量的商品id
            createQueue(good.getId(),good.getStockCount());
        }

    }

    private void createQueue(Long id, Integer stockCount) {

        if(stockCount > 0){
            for (int i=0;i<stockCount;i++){
                redisTemplate.boundListOps(SystemConst.CONST_SECKILLGOODS_ID_PREFIX+id)
                        .leftPush(id);
            }
        }
    }

}
