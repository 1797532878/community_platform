package com.community.platform.controller;

import com.community.platform.entity.User;
import com.community.platform.service.LikeService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int userId,int entityUserId) {
        //  用户
        User user = userService.findUserById(userId);

        // 点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return CommunityUtil.getJSONString(0,"ok",map);
    }

}
