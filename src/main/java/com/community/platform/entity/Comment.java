package com.community.platform.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {

    private int id;
    private int userId;
    //评论目标的类别  1帖子  2评论 3用户 4代表题 5课程
    private int entityType;
    //具体贴子ID
    private int entityId;
    //指向某个人的评论
    private int targetId;
    private String content;
    //状态0 正常 1不生效
    private int status;
    private Date createTime;

}
