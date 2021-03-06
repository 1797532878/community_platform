package com.community.platform.dao;


import com.community.platform.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
//  已经使用redis存储进行优化
public interface LoginTicketMapper {

    //自动生成主键要注明
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    //自动生成主键
    int insertLoginTicket(LoginTicket loginTicket);

    //ticket用于标识用户，发给浏览器
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket = #{ticket} ",
            "<if test=\"ticket != null \">",
            "and 1=1",
            "</if>",
            "</script>"
    })
    //如果需要if
    int updateStatus(String ticket,int status);

}
