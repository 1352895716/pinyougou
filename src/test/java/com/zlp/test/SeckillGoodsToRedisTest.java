package com.zlp.test;

import com.zlp.mapper.TbSeckillGoodsMapper;
import com.zlp.pojo.TbSeckillGoods;
import com.zlp.pojo.TbSeckillGoodsExample;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: SeckillGoodsToRedisTest
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/18 19:33
 * @Version 1.0
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring/applicationContext-*.xml")
public class SeckillGoodsToRedisTest {

    @Resource
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private RedisTemplate redisTemplate;
    @Test
    public void testTask() throws IOException {
        while(true){
            System.in.read();
        }
    }
    @Test
    public void test(){
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

        }

    }

}
