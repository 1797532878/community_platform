package com.community.platform.dao;

import com.community.platform.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    int selectCountByEntityAndUserId(int entityType,int userId);

    List<Comment> selectCommentByEntityAndUserId(int entityType,int userId,int offset,int limit);

    List<Comment> selectCommentByEntityId(int entityId);
}
