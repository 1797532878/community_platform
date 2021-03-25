package com.community.platform.dao;

import com.community.platform.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     *
     * @param userId userId不为0的时候才拼入sql
     * @param offset 起始行的行号
     * @param limit 一页多少数据
     * @return
     */
    List<DiscussPost> selectDiscussPost(int userId,int offset,int limit);

    /**
     *    如果只有一个参数 且参数要参与动态拼接sql  必须加@Param  同时也是起别名的作用
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 插入帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);
    


}
