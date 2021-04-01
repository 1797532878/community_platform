package com.community.platform.service;

import com.community.platform.dao.LoginTicketMapper;
import com.community.platform.dao.UserMapper;
import com.community.platform.entity.LoginTicket;
import com.community.platform.entity.User;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import com.community.platform.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    //注入Thymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById (int id) {
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){

        Map<String,Object> map =  new HashMap<>();

        //空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("errorMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("errorMsg","密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("errorMsg","密码不能为空!");
            return map;
        }
        if (StringUtils.length(user.getPassword()) < 8){
            map.put("errorMsg","密码不能少于8位");
            return map;
        }
        //验证账号是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("errorMsg","该账号已存在");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("errorMsg","该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("username",user.getUsername());
        //http://localhost:8080/activation/101/code
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.senMail(user.getEmail(),"激活账号",content);


        return map;
    }

    //返回激活状态
    public  int  activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
//            clearCache(userId);//清理redis缓存
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    //登录
    public Map<String,Object> login(String username,String password,int expiredSeconds) {

        Map<String,Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("errorMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("errorMsg","密码不能为空!");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("errorMsg","该账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("errorMsg","该账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("errorMsg","密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    //退出
    public void logout(String ticket) {
            loginTicketMapper.updateStatus(ticket,1);
    }

    //查询登陆凭证
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    //更新头像路径
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId,headerUrl);
    }

    public int updatePassword(int userId, String password) {
        return userMapper.updatePassword(userId,password);
    }

    public User findUserByName (String username) {
        return userMapper.selectByName(username);
    }
}
