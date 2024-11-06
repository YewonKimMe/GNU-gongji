package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    // TODO 이게문제네 리스트 생성하고 거기다 또 넣으니 이러지
    public <T> void saveList(String key, List<T> list) {
        redisTemplate.opsForList().rightPushAll(key, list);
    }

    public <T> List<T> getAllListItems(String key, Class<T> cls) {
        List<Object> redisResult = redisTemplate.opsForList().range(key, 0, -1);

        log.debug("redisResult={}", redisResult);

        if (redisResult == null) return null;

        log.debug("redisResult.get(0)={}", redisResult.get(0));

        return (List<T>) redisResult.get(0);
    }
}
