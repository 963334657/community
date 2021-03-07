package com.community;

import com.community.entity.DiscussPost;
import com.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTeats {

    @Autowired
    private DiscussPostService discussPostService;


    @Test
    public void initDataForTest() {
        for (int i = 0; i < 100000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("论坛性能测试专用数据");
            post.setContent("增加十万条数据来试试看对访问数据库有什么影响！！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 100);
            discussPostService.addDiscussPost(post);
        }
    }
    @Test
    public void testCache(){
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,0));
    }
}
