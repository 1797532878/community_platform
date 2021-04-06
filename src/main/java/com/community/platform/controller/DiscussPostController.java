package com.community.platform.controller;

import com.community.platform.entity.*;
import com.community.platform.service.CommentService;
import com.community.platform.service.DiscussPostService;
import com.community.platform.service.LikeService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(HttpServletRequest request, String title, String content) {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(200,"发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.POST)
    @ResponseBody
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,@RequestBody Page page,
                                 HttpServletRequest request,@CookieValue(name = "ticket",required = false) String ticket) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        // 作者
        User user = userService.findUserById(post.getUserId());

        // 赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        // 赞状态
        HttpSession session = request.getSession();
        // 用户
        User user1 = (User) session.getAttribute("user");
        LoginTicket loginTicket = null;
        int likeStatus = 0;
        if (ticket != null){
            loginTicket = userService.findLoginTicket(ticket);

            // 已退出 或者 没有登录
            if (loginTicket.getStatus() == 1 || user1 == null) {
                likeStatus = 0;
            } else {
                likeStatus = likeService.findEntityLikeStatus(user1.getId(),ENTITY_TYPE_POST,discussPostId);
            }
        }

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);


        //评论的分页信息
        page.setPath("http://localhost:8081/communityPlatform/discuss/detail/" + discussPostId);
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        //帖子评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论
                Map<String,Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                //评论者的信息
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                // 赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                likeStatus = 0;
                if (ticket != null) {
                    // 赞状态
                    if (loginTicket.getStatus() == 1 || user1 == null) {
                        likeStatus = 0;
                    } else {
                        likeStatus = likeService.findEntityLikeStatus(user1.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                    }
                }
                commentVo.put("likeStatus",likeStatus);

                //评论的回复
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String,Object> replyVo  = new HashMap<>();
                        // 回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("targetUser",targetUser);

                        // 赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        // 赞状态
                        likeStatus = 0;
                        if (ticket != null) {
                            if (loginTicket.getStatus() == 1 || user1 == null) {
                                likeStatus = 0;
                            } else {
                                likeStatus = likeService.findEntityLikeStatus(user1.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                            }
                        }
                        replyVo.put("likeStatus",likeStatus);

                        //给List装入map
                        replyVoList.add(replyVo);
                    }
                }
                // 将回复放入评论
                commentVo.put("replies",replyVoList);
                // 将回复的数量放入评论
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVOList.add(commentVo);
            }
        }

        map.put("user",user);
        map.put("post",post);
        map.put("comment",commentVOList);
        map.put("page",page);
        return CommunityUtil.getJSONString(200,"ok",map);
    }

}
