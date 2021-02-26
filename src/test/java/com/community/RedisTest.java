package com.community;

import com.community.dao.DiscussPostMapper;
import com.community.dao.LoginTicketMapper;
import com.community.dao.MessageMapper;
import com.community.dao.UserMapper;
import com.community.entity.DiscussPost;
import com.community.entity.LoginTicket;
import com.community.entity.Message;
import com.community.entity.User;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey="test:count";
        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey="test:user";
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");
        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists(){
        String redisKey="test:ids";
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets(){
        String redisKey="test:teachers";
        redisTemplate.opsForSet().add(redisKey,"aa","bb","cc","dd","ee");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets(){
        String redisKey="test:students";
        redisTemplate.opsForZSet().add(redisKey,"aa",80);
        redisTemplate.opsForZSet().add(redisKey,"bb",70);
        redisTemplate.opsForZSet().add(redisKey,"cc",60);
        redisTemplate.opsForZSet().add(redisKey,"dd",50);
        redisTemplate.opsForZSet().add(redisKey,"ee",40);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"aa"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"bb"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        System.out.println(redisTemplate.expire("test:students",10, TimeUnit.SECONDS));
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String redisKey="test:count";
        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String redisKet="test:hll:01";
        for (int i=1;i<100000;i++){
            redisTemplate.opsForHyperLogLog().add(redisKet,i);
        }
        for (int j=1;j<100000;j++){
            int r=(int)(Math.random()*100000);
            redisTemplate.opsForHyperLogLog().add(redisKet,r);
        }

        long size=redisTemplate.opsForHyperLogLog().size(redisKet);
        System.out.println(size);
    }
    //将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion(){
        String rediskey2="test:hll:02";
        for (int i=1;i<10000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey2,i);
        }
        String rediskey3="test:hll:03";
        for (int i=5001;i<15000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey3,i);
        }
        String rediskey4="test:hll:04";
        for (int i=10001;i<20000;i++){
            redisTemplate.opsForHyperLogLog().add(rediskey4,i);
        }

        String unionkey="test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionkey,rediskey2,rediskey3,rediskey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionkey));
    }
    //统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String rediskey="test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(rediskey,1,true);
        redisTemplate.opsForValue().setBit(rediskey,4,true);
        redisTemplate.opsForValue().setBit(rediskey,7,true);
        redisTemplate.opsForValue().setBit(rediskey,8,true);
        redisTemplate.opsForValue().setBit(rediskey,9,true);
        redisTemplate.opsForValue().setBit(rediskey,11,true);
        redisTemplate.opsForValue().setBit(rediskey,15,true);
        redisTemplate.opsForValue().setBit(rediskey,16,true);
        redisTemplate.opsForValue().setBit(rediskey,17,true);
        redisTemplate.opsForValue().setBit(rediskey,19,true);
        redisTemplate.opsForValue().setBit(rediskey,20,true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,0));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,3));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,4));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,11));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,21));

        //统计
        Object obj=redisTemplate.execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                 return redisConnection.bitCount(rediskey.getBytes());
            }
        });
        System.out.println(obj);
    }
    //统计3组数据的布尔值，并对这3组数据做or运算
    @Test
    public void testBitMapOperation(){
        String rediskey2="test:bm:02";
        redisTemplate.opsForValue().setBit(rediskey2,0,true);
        redisTemplate.opsForValue().setBit(rediskey2,1,true);
        redisTemplate.opsForValue().setBit(rediskey2,2,true);
        String rediskey3="test:bm:03";
        redisTemplate.opsForValue().setBit(rediskey3,3,true);
        redisTemplate.opsForValue().setBit(rediskey3,1,true);
        redisTemplate.opsForValue().setBit(rediskey3,2,true);
        String rediskey4="test:bm:04";
        redisTemplate.opsForValue().setBit(rediskey4,3,true);
        redisTemplate.opsForValue().setBit(rediskey4,4,true);
        redisTemplate.opsForValue().setBit(rediskey4,2,true);
        String rediskeyOpr="test:bm:or";
        Object obj=redisTemplate.execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        rediskeyOpr.getBytes(),rediskey2.getBytes(),rediskey3.getBytes(),rediskey4.getBytes());
                return redisConnection.bitCount(rediskeyOpr.getBytes());
            }
        });
        System.out.println(obj);
    }
}

























