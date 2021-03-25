package com.community.platform.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");//classes
             //字节流转字符流转缓冲流
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));)
        {
            String keyword;
            while ((keyword = reader.readLine()) != null){
                this.addKeyword(keyword);
            }

        } catch (Exception e) {
            logger.error("加载敏感词失败： " + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword){
        TrieNode tempNodes = rootNode;
        for (int i = 0;i < keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNodes.getSubNode(c);
            //没有节点
            if (subNode == null){
                //初始化节点
                subNode = new TrieNode();
                //加入树中
                tempNodes.addSubNode(c,subNode);
            }

            //指向子节点，进入下一轮循环
            tempNodes = subNode;

            //设置结束标识
            if (i == keyword.length() - 1){
                tempNodes.setKeywordEnd(true);
            }
        }


    }

    /**
     *
     * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        StringBuilder stringBuilder = new StringBuilder();

        while (begin < text.length()) {
            if (position < text.length()) {
                char c = text.charAt(position);

                //跳过符号
                if (isSymbol(c)) {
                    //若指针1处于根节点，将此符号计入结果，让指针2向下走 一步
                    if (tempNode == rootNode) {
                        stringBuilder.append(c);
                        begin++;
                    }
                    //无论符号在开头或中间，指针3都向下走一步
                    position++;
                    continue;
                }
                //检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    //没有在树种找到这个字
                    //以begin开头的字符串不是敏感词
                    stringBuilder.append(text.charAt(begin));
                    //进入下一个位置
                    position = ++begin;
                    //重新指向根节点
                    tempNode = rootNode;
                } else if (tempNode.isKeywordEnd()) {
                    //发现了敏感词，将begin到position字符串替换
                    stringBuilder.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = rootNode;
                } else {
                    position++;
                }
            } else {
                //position越界了还是没发现敏感词
                stringBuilder.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return stringBuilder.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //0X2E80---0x9FFF东亚文字范围  中文 日文等
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0X2E80 || c > 0x9FFF);//是否普通字符 123等 true
    }

    //前缀树
    private class TrieNode{

        //关键词结束的标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);//map
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

}
