package com.project.legendsofleague.domain.membercoupon.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final String REDIS_KEY_PREFIX = "coupon:";

    public Long increaseMemberCouponCount(Long couponId) {
        String key = getMemberCouponRedisKey(couponId);

        return redisTemplate
            .opsForValue()
            .increment(key);
    }

    public Long decreaseMemberCouponCount(Long couponId) {
        String key = getMemberCouponRedisKey(couponId);

        return redisTemplate
            .opsForValue()
            .decrement(key);
    }

    public Map<Long, Long> getMemberCouponCounts() {
        Map<Long, Long> memberCouponCountMap = new HashMap<>();
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        Optional.ofNullable(keys).ifPresent(keyList -> {
            keyList.forEach(key -> {
                Long couponId = parseMemberCouponRedisKey(key);
                Long count = Long.valueOf(
                    Optional.ofNullable(redisTemplate.opsForValue().get(key))
                        .orElse("0"));
                memberCouponCountMap.put(couponId, count);
            });
        });

        return memberCouponCountMap;
    }

    public void deleteMemberCouponCount() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        Optional.ofNullable(keys).ifPresent(keyList -> {
            keyList.forEach(redisTemplate::delete);
        });
    }

    private String getMemberCouponRedisKey(Long couponId) {
        return REDIS_KEY_PREFIX + couponId;
    }

    private Long parseMemberCouponRedisKey(String key) {
        return Long.valueOf(key.split(":")[1]);
    }


}
