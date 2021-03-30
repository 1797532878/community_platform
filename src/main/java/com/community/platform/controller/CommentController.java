package com.community.platform.controller;

import com.community.platform.entity.Comment;
import com.community.platform.entity.User;
import com.community.platform.service.CommentService;
import com.community.platform.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    @ResponseBody
    public String addComment(@PathVariable("discussPostId") int discussPostId,String content,String entityType,String entityId,String targetId, String userId) {
        if (StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(400,"内容不能为空!");
        }
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTargetId(Integer.parseInt(targetId));
        comment.setEntityId(Integer.parseInt(entityId));
        comment.setEntityType(Integer.parseInt(entityType));
        comment.setUserId(Integer.parseInt(userId));
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        Map<String,Object> map = new HashMap<>();
        map.put("discussPostId",discussPostId);
        return CommunityUtil.getJSONString(200,"ok",map);
    }

}
