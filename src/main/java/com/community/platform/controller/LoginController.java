package com.community.platform.controller;

import com.community.platform.entity.LoginTicket;
import com.community.platform.entity.User;
import com.community.platform.service.MessageService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import com.community.platform.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageService messageService;


    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestBody User user){
        Map<String,Object> map = userService.register(user);
        System.out.println(user.toString());
        if (map == null || map.isEmpty()){
            map.put("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            return CommunityUtil.getJSONString(200,"success",map);
        }else {
            return CommunityUtil.getJSONString(400,"error",map);
        }
    }

    //激活
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code ){
        int result = userService.activation(userId,code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","http://localhost:8080/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已经激活！");
            model.addAttribute("target","http://localhost:8080");
        }else {
            model.addAttribute("msg","激活失败，您提供的激活码错误！");
            model.addAttribute("target","http://localhost:8080");
        }
        return "/site/operate-result";
    }

    // 优化： 使用redis 存储验证码
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

//        //将验证码存入session  存在跨域问题需要设置axios
//        session.setAttribute("kaptcha",text);

        // 使用redis
        String kaptchaOwner = CommunityUtil.generateUUID();
         Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
         cookie.setMaxAge(60);
         cookie.setPath(contextPath);
         response.addCookie(cookie);

         // redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 优化:使用redis存储验证码 不使用session
    //登录
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam(value = "username") String username,@RequestParam(value = "password") String password,
                        @RequestParam(value = "code") String code,@RequestParam(value = "rememberme",required = false) String rememberme,
                        /*HttpSession session,*/HttpServletResponse response,@CookieValue("kaptchaOwner") String kaptchaOwner) {
        Map<String,Object> map = new HashMap<>();
        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");

        // 使用redis
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }


        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            map.put("errorMsg","验证码不正确!");
            return CommunityUtil.getJSONString(400,"验证码错误",map);
        }
        //检查账号密码
        int expiredSeconds = rememberme.equals("true") ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map1 = userService.login(username,password,expiredSeconds);
        if (map1.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket",map1.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return CommunityUtil.getJSONString(200);
        }else {
            return CommunityUtil.getJSONString(400,"登录错误",map1);
        }
    }

    //退出
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    @ResponseBody
    public String logout(@CookieValue("ticket") String ticket, HttpSession session, SessionStatus sessionStatus){
        userService.logout(ticket);
        session.invalidate();
        sessionStatus.setComplete();
        return CommunityUtil.getJSONString(200);
    }

    @RequestMapping(path = "/confirmLogin",method = RequestMethod.GET)
    @ResponseBody
    public String confirmLogin (@CookieValue(name = "ticket",required = false) String ticket, HttpServletRequest request) {
        LoginTicket loginTicket = userService.findLoginTicket(ticket);
        if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
            User user = userService.findUserById(loginTicket.getUserId());
            Map<String,Object> map = new HashMap<>();
            map.put("user",user);
            //设置已登录
            HttpSession session = request.getSession();
            session.setAttribute("user",user);

            // 请求时查询未读消息数量
            int letterUnreadTotalCount = messageService.findLetterUnreadCount(user.getId(),null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);

            map.put("unreadCount",letterUnreadTotalCount + noticeUnreadCount);

            return CommunityUtil.getJSONString(200,"已登录",map);
        }else {
            return CommunityUtil.getJSONString(400,"没有登录");
        }
    }

}
