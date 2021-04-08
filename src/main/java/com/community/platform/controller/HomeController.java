package com.community.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.community.platform.entity.DiscussPost;
import com.community.platform.entity.Page;
import com.community.platform.entity.User;
import com.community.platform.service.DiscussPostService;
import com.community.platform.service.LikeService;
import com.community.platform.service.MessageService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    @ResponseBody
    public String getIndexPage() {
        Page page = new Page();
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("http://localhost:8081/communityPlatform/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,0,10);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                map.put("page",page);
                // 查询点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }

        JSONObject jsonObject = new JSONObject();
        return jsonObject.toJSONString(discussPosts);
    }
    //post请求
    @RequestMapping(path = "/index",method = RequestMethod.POST)
    @ResponseBody
    public String getPage(@RequestBody Page page) {

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("http://localhost:8081/communityPlatform/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                map.put("page",page);
                discussPosts.add(map);
            }
        }
        JSONObject jsonObject = new JSONObject();
        return jsonObject.toJSONString(discussPosts);
    }

}
