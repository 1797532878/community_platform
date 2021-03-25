//package com.community.platform.config;
//
//
//import com.community.platform.controller.Interceptor.LoginRequiredInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;
//
////    @Autowired
////    private LoginTicketInterceptor loginTicketInterceptor;
////
////    @Autowired
////    private AlphaInterceptor alphaInterceptor;
////
////    @Override
////    public void addInterceptors(InterceptorRegistry registry) {
////        registry.addInterceptor(loginTicketInterceptor)
////                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");
////        registry.addInterceptor(alphaInterceptor)
////                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg")
////                .addPathPatterns("/register", "/login");
////    }
//
////    @Override
////    public void addInterceptors(InterceptorRegistry registry) {
////        registry.addInterceptor(loginRequiredInterceptor)
////                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");
////    }
//}
