//package com.community.platform.controller.Interceptor;
//
//import com.community.platform.annotation.LoginRequired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Method;
//
//@Component
//public class LoginRequiredInterceptor implements HandlerInterceptor {
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (handler instanceof HandlerMethod){
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod();
//            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
//            Cookie[] cookies = request.getCookies();
//            Cookie cookie = null;
//            for (Cookie cookie1 :cookies) {
//                if (cookie1.getName().equals("ticket")) {
//                    cookie = cookie1;
//                }
//            }
//            if (loginRequired != null && cookie == null) {
//                response.sendRedirect("http://localhost:8080/login");
//                return false;
//            }
//        }
//
//        return true;
//    }
//}
