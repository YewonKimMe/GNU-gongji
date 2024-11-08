package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public <T> void saveList(String key, List<T> list) {
        redisTemplate.opsForList().rightPushAll(key, list.toArray());
    }

    public <T> List<T> getAllListItems(String key, Class<T> cls) {
        List<Object> redisResult = redisTemplate.opsForList().range(key, 0, -1);

        log.debug("redisResult={}", redisResult);

        if (redisResult == null || redisResult.isEmpty()) return null;

        return (List<T>) redisResult;
    }
}
