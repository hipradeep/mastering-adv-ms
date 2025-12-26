package com.hipradeep.userservice.repository;

import com.hipradeep.userservice.entity.User;
import com.hipradeep.userservice.entity.User2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserDao {

    public static final String HASH_KEY = "USER";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public User2 save(User2 user) {
        redisTemplate.opsForHash().put(HASH_KEY, user.getId().toString(), user);
        return user;
    }

    public List<User2> findAll() {
        return redisTemplate.opsForHash().values(HASH_KEY).stream()
                .map(obj -> (User2) obj)
                .collect(Collectors.toList());
    }

    public User2 findById(Long id) {
        return (User2) redisTemplate.opsForHash().get(HASH_KEY, id.toString());
    }

    public String delete(Long id) {
        redisTemplate.opsForHash().delete(HASH_KEY, id.toString());
        return "User removed from redis !!";
    }
}
