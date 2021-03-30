package com.community.platform.controller;

import com.community.platform.entity.Message;
import com.community.platform.entity.Page;
import com.community.platform.entity.User;
import com.community.platform.service.MessageService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.POST)
    @ResponseBody
    public String getLetterList(@RequestBody Page page, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {

            page.setLimit(5);
            page.setPath("/letter/list");
            page.setRows(messageService.findConversationsCount(user.getId()));
            //会话列表
            List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
            List<Map<String,Object>> conversations = new ArrayList<>();
            //遍历conversationList 同时给map中添加页面需要的其他信息
            if (conversationList != null) {
                for (Message message : conversationList) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("conversation",message);
                    map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                    map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                    int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                    map.put("targetUser",userService.findUserById(targetId));
                    //放入最后的list
                    conversations.add(map);
                }
            }
            //查询未读消息总数
            int letterUnreadTotalCount = messageService.findLetterUnreadCount(user.getId(),null);
            Map<String,Object> map1 = new HashMap<>();
            map1.put("letterUnreadTotalCount",letterUnreadTotalCount);
            map1.put("conversations",conversations);
            map1.put("page",page);
            return CommunityUtil.getJSONString(200,"ok",map1);
        }
        return CommunityUtil.getJSONString(400,"未登录");
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.POST)
    @ResponseBody
    public String getLetterDetail (@PathVariable String conversationId,@RequestBody Page page,HttpServletRequest request) {
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" +  conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());

        List<Map<String,Object>> letters = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        Map<String,Object> map1 = new HashMap<>();
        map1.put("letters",letters);
        map1.put("page",page);
        //获取目标用户id
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if ( user != null ) {
            User targetUser;
            if (user.getId() == id0) {
                targetUser = userService.findUserById(id1);
            }else {
                targetUser = userService.findUserById(id0);
            }
            map1.put("targetUser",targetUser);
        }

        return CommunityUtil.getJSONString(200,"ok",map1);
    }

}
