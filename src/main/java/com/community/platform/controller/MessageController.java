package com.community.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.community.platform.entity.Message;
import com.community.platform.entity.Page;
import com.community.platform.entity.User;
import com.community.platform.service.MessageService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

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
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
            Map<String,Object> map1 = new HashMap<>();
            map1.put("letterUnreadTotalCount",letterUnreadTotalCount);
            map1.put("conversations",conversations);
            map1.put("page",page);
            map1.put("noticeUnreadCount",noticeUnreadCount);
            return CommunityUtil.getJSONString(200,"ok",map1);
        }
        return CommunityUtil.getJSONString(400,"未登录");
    }

    // 私信详情
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
        // 私信目标
        if ( user != null ) {
            User targetUser;
            if (user.getId() == id0) {
                targetUser = userService.findUserById(id1);
            }else {
                targetUser = userService.findUserById(id0);
            }
            map1.put("targetUser",targetUser);
        }

        // 设置消息为已读
        List<Integer> letterIds = getLetterIds(letterList, user);
        if (!letterIds.isEmpty()){
            messageService.readMessage(letterIds);
        }

        return CommunityUtil.getJSONString(200,"ok",map1);
    }

    // 遍历出未读消息
    private List<Integer> getLetterIds(List<Message> letterList,User user) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (user.getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    //发送私信
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter (String toName,String content,HttpServletRequest request) {
        User targetUser = userService.findUserByName(toName);
        if (targetUser == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        if (StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(2,"内容不能为空！");
        }
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(targetUser.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);

    }
    //删除私信
    @RequestMapping(path = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(String conversationId,String content){
        messageService.dleMessage(conversationId,content);
        return CommunityUtil.getJSONString(0);

    }

    // 通知
    @RequestMapping(path = "/notice/list",method = RequestMethod.POST)
    @ResponseBody
    public String getNoticeList(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        Map<String,Object> map = new HashMap<>();

        // 查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);

        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            // 触发的人
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("unread",unread);
            map.put("commentNotice",messageVO);
        }

        // 查询点赞类的通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);

        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            // 触发的人
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVO.put("unread",unread);
            map.put("likeNotice",messageVO);
        }

        // 查询关注类的通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            // 触发的人
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("unread",unread);
            map.put("followNotice",messageVO);
        }


        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        map.put("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        map.put("noticeUnreadCount",noticeUnreadCount);

        return CommunityUtil.getJSONString(0,"noticeList",map);
    }

    @PostMapping("/notice/detail/{topic}")
    @ResponseBody
    public String getNoticeDetail(@PathVariable("topic") String topic,@RequestBody Page page,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));
        List<Message> noticeList = messageService.findNotices(user.getId(),topic,page.getOffset(),page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String,Object> map = new HashMap<>();
                // 通知
                map.put("notice",notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
                // 目标用户  页面需要填充的信息
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                // 通知来源用户 这里是系统SYSTEM
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("notices",noticeVoList);
        map.put("page",page);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList,user);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return CommunityUtil.getJSONString(0,"notices",map);
    }
}
