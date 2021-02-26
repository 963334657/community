package com.community;

import com.community.dao.DiscussPostMapper;
import com.community.dao.LoginTicketMapper;
import com.community.dao.MessageMapper;
import com.community.dao.UserMapper;
import com.community.entity.DiscussPost;
import com.community.entity.LoginTicket;
import com.community.entity.Message;
import com.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectConversation(){
        List<Message> list =messageMapper.selectConversation(111,0,20);
        for(Message msg:list){
            System.out.println(msg);
        }
    }
    @Test
    public void testSelectConversationCount(){
        System.out.println(messageMapper.selectConversationCount(111));
    }
    @Test
    public void testSelectLetters(){
        List<Message> list =messageMapper.selectLetters("111_112",0,20);
        for(Message msg:list){
            System.out.println(msg);
        }
    }
    @Test
    public void testSelectLetterCount(){
        System.out.println(messageMapper.selectLetterCount("111_112"));
    }
    @Test
    public void testSelectLetterUnreadCount(){
        System.out.println(messageMapper.selectLetterUnreadCount(131,"111_131"));
    }

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }
    @Test
    public void testSelectPosts(){
        List<DiscussPost> list=discussPostMapper.selectDiscussPosts(149,0,10,0);
        for(DiscussPost post:list){
            System.out.println(post);
        }
        int rows=discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("ABC");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        int rows=loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(rows);
    }

    @Test
    public void testSelectByTicket(){
        LoginTicket loginTicket=loginTicketMapper.selectByTicket("ABC");
        System.out.println(loginTicket.toString());
    }
    @Test
    public void testUpdateStatus(){
        int rows=loginTicketMapper.updateStatus(1,"ABC");
        System.out.println(rows);
    }
}
