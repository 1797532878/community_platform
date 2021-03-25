//package com.community.platform.controller.Interceptor;
//
//import com.community.platform.entity.LoginTicket;
//import com.community.platform.entity.User;
//import com.community.platform.service.UserService;
//import com.community.platform.util.CookieUtil;
//import com.community.platform.util.HostHolder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Date;
//
//@Component
//public class LoginTicketInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private HostHolder hostHolder;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 从cookie中获取凭证
//        String ticket = CookieUtil.getValue(request,"ticket");
//        System.out.println("preHandle---ticket" + ticket);
//        if (ticket != null) {
//            //查询凭证
//            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            // 检查凭证是否有效
//            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
//                //根据凭证查询用户
//                User user = userService.findUserById(loginTicket.getUserId());
//                // 在本次请求中持有用户
//                hostHolder.setUser(user);
//                System.out.println("preHandle----user:" + user);
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        hostHolder.clear();
//    }
//}
