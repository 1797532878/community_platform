package com.community.platform.dao;

import com.community.platform.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id,int status);

    //更新头像
    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);
}
