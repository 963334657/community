package com.community;

import com.community.Util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import javax.naming.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sentMail("963334657@qq.com","test","hello");
    }

    @Test
    public void testHtmlMail(){
        org.thymeleaf.context.Context context=new org.thymeleaf.context.Context();
        context.setVariable("username","sunday");

        String content=templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sentMail("963334657@qq.com","html",content);
    }
}
