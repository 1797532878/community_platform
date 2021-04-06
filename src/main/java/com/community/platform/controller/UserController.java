package com.community.platform.controller;

import com.community.platform.entity.LoginTicket;
import com.community.platform.entity.User;
import com.community.platform.service.FollowService;
import com.community.platform.service.LikeService;
import com.community.platform.service.UserService;
import com.community.platform.util.CommunityConstant;
import com.community.platform.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    //上传头像   必须要登录
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public String uploadHeader(MultipartFile headerImage,String userId) {
        Map<String,Object> map = new HashMap<>();
        //MultipartFile可以设置为数组
        System.out.println("userId:" + userId);
        System.out.println("headerImage" + headerImage);
        if (headerImage == null) {
            map.put("errorMsg","上传图片为空");
            return CommunityUtil.getJSONString(400,"error",map);
        }

        String filename = headerImage.getOriginalFilename();
        if (filename != null){
            String suffix = filename.substring(filename.lastIndexOf("."));
            if (StringUtils.isBlank(suffix)) {
                map.put("errorMsg","文件格式不正确");
                return CommunityUtil.getJSONString(400,"error",map);
            }
            //生成随机的文件名
            filename = CommunityUtil.generateUUID() + suffix;
            System.out.println("文件名：" + filename);
        }

        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("上传文件失败，服务器异常",e);
        }
        //更新当前用户的头像的路径 (web访问路径)
        //http://localhost:8081/communityPlatform/user/header/xxx.png
//        User user = userService.findUserById(userId);
        //外部访问路径
        String headerUrl = domain + "/user/header/" + filename;
        //更新头像地址为电脑上的地址   服务器加载头像时会自动请求这个地址  下面的getHeader处理这个地址的请求给服务器图片
            int id = Integer.parseInt(userId);
           userService.updateHeader(id,headerUrl);

        return CommunityUtil.getJSONString(200,"ok");
    }

    //获取头像
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ( (b = fis.read(buffer)) != -1) {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //修改账号密码
    @RequestMapping(path = "/password",method = RequestMethod.POST)
    @ResponseBody
    public String modifyPassword(String userId,String old_password,String new_password){
        Map<String,Object> map = new HashMap<>();

        if (StringUtils.isBlank(userId)) {
            map.put("errorMsg","账户未登录！");
            return CommunityUtil.getJSONString(400,"error");
        }
        int id = Integer.parseInt(userId);
        User user = userService.findUserById(id);
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(new_password)) {
            map.put("errorMsg","密码不能为空！");
            return CommunityUtil.getJSONString(400,"error",map);
        }
        if (CommunityUtil.md5(old_password + user.getSalt()).equals(user.getPassword())){
            userService.updatePassword(id,CommunityUtil.md5(new_password + user.getSalt()));
            return CommunityUtil.getJSONString(200,"ok");
        }else {
            map.put("errorMsg","原始密码不正确!");
            System.out.println(old_password);
            System.out.println(new_password);
            System.out.println(user.getPassword());
            System.out.println(CommunityUtil.md5(old_password + user.getSalt()));
            return CommunityUtil.getJSONString(400,"errorMsg",map);
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.POST)
    @ResponseBody
    public String getProfilePage (@PathVariable("userId") int userId,HttpServletRequest request) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        Map<String,Object> map = new HashMap<>();
        map.put("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        map.put("likeCount",likeCount);

        // 关注数量
        // 他 关注的 数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        map.put("followeeCount",followeeCount);
        map.put("followerCount",followerCount);
        // 是否已关注
        boolean hasFolllowed = false;
        User hoder = (User) request.getSession().getAttribute("user");
        if (hoder != null) {
            // 我 关注 他
            hasFolllowed = followService.hasFollowed(hoder.getId(),ENTITY_TYPE_USER,userId);
        }
        map.put("hasFollowed",hasFolllowed);
        return CommunityUtil.getJSONString(0,"ok",map);
    }
}
