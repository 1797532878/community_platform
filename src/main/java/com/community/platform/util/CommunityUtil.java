package com.community.platform.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //生成激活码等随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    //只能加密不能解密  hello -> abc123def456  每次都是
    //hello  + 3e4a8 -> abc123def456abc  提高安全性
    public static String md5(String key){
        //null 为空 空串为空  空值为空
        if (key.isEmpty() || key.equals("")){
            return null;
        }
        //使用Spring的工具加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for (String key : map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code,String msg){
        return getJSONString(code,msg,null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

}
