package com.community.platform.service;

import com.community.platform.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *
     * @param userId 用户ID
     * @param entityType 被点赞的实体类型
     * @param entityId  被点赞的实体ID
     * @param entityUserId  被点赞的人的ID
     */
    public void like(int userId,int entityType, int entityId, int entityUserId) {
        // 同时操作给user和实体地点赞需要注意事务性
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                // 以某一实体的Id为key 统计该实体获得的赞
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // redis事务中 查询要放在事务之外  事务中 是把所有的命令放在一个队列当中 当提交时统一执行
                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                // 开始事务
                operations.multi();

                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                // 结束事务
                return operations.exec();
            }
        });
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人是否点赞
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount (int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

}
