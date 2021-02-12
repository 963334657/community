package com.community;

import com.community.Util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void textSensitiveFilter(){
        String text="这里可以嫖娼，可以吸。毒，可以开/票，可以赌；博，哈哈哈！";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
