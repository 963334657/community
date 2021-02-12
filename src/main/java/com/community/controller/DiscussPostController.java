package com.community.controller;


import com.community.Util.CommunityConstant;
import com.community.Util.CommunityUtil;
import com.community.Util.HostHolder;
import com.community.entity.Comment;
import com.community.entity.DiscussPost;
import com.community.entity.Page;
import com.community.entity.User;
import com.community.service.CommentService;
import com.community.service.DiscussPostService;
import com.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jws.WebParam;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant{
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user=hostHolder.getUser();
        if (user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }
        DiscussPost discussPost=new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //作者
        User user=userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());


        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList=commentService.findCommenmtByEntity(ENTITY_TYPE_POST,discussPost.getId(),page.getOffset(),page.getLimit());
        //评论VO列表
        List<Map<String,Object>> commentVOList=new ArrayList<>();
        if(commentVOList!=null){
            for (Comment comment:commentList){
                //评论VO
                Map<String,Object> commentVO=new HashMap<>();
                //评论
                commentVO.put("comment",comment);
                //作者
                commentVO.put("user",userService.findUserById(comment.getUserId()));

                //回复列表
                List<Comment> repltList=commentService.findCommenmtByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> repltVOList=new ArrayList<>();
                if(repltList!=null){
                    for(Comment reply:repltList){
                        Map<String,Object> replyVO=new HashMap<>();
                        //回复
                        replyVO.put("reply",reply);
                        //作者
                        replyVO.put("user",userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVO.put("target",target);

                        repltVOList.add(replyVO);
                    }
                }
                commentVO.put("replys",repltVOList);
                //回复数量
                int replyCount= commentService.findCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("replyCount",replyCount);

                commentVOList.add(commentVO);
            }
        }

        model.addAttribute("comments",commentVOList);
        return "/site/discuss-detail";
    }
}



























