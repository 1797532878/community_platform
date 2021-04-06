package com.community.platform.controller;

import com.community.platform.entity.Page;
import com.community.platform.entity.User;
import com.community.platform.service.FollowService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String Follow(int entityType, int entityId, int loginUserId) {

        followService.follow(loginUserId,entityType,entityId);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType,int entityId,int loginUserId) {
        followService.unfollow(loginUserId,entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @PostMapping("/followees/{userId}")
    @ResponseBody
    public String getFollowees(@PathVariable("userId")int userId,@RequestBody Page page, HttpServletRequest request) {
        Map<String,Object> map = new HashMap<>();
        User user =  userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        map.put("user",user);
        // 分页信息设置
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if (userList != null) {
            for (Map<String,Object> map1 : userList) {
                User u = (User) map1.get("user");
                map1.put("hasFollowed",hasFollowed(u.getId(), (User) request.getSession().getAttribute("user")));
            }
        }
        map.put("followees",userList);
        map.put("page",page);
        return CommunityUtil.getJSONString(0,"followees",map);
    }

    @PostMapping("/followers/{userId}")
    @ResponseBody
    public String getFollowers(@PathVariable("userId")int userId,@RequestBody Page page, HttpServletRequest request) {
        Map<String,Object> map = new HashMap<>();
        User user =  userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        map.put("user",user);
        // 分页信息设置
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if (userList != null) {
            for (Map<String,Object> map1 : userList) {
                User u = (User) map1.get("user");
                map1.put("hasFollowed",hasFollowed(u.getId(), (User) request.getSession().getAttribute("user")));
            }
        }
        map.put("followers",userList);
        map.put("page",page);
        return CommunityUtil.getJSONString(0,"followres",map);
    }

    private boolean hasFollowed(int userId,User user) {
        if (user == null) {
            return false;
        }
        return followService.hasFollowed(user.getId(),ENTITY_TYPE_USER,userId);
    }
}
