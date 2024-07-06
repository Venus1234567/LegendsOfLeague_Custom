package com.project.legendsofleague.scheduler;

import com.project.legendsofleague.domain.coupon.domain.Coupon;
import com.project.legendsofleague.domain.coupon.repository.CouponRepository;
import com.project.legendsofleague.domain.membercoupon.repository.MemberCouponRedisRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponStockScheduler {

    private final CouponRepository couponRepository;

    private final MemberCouponRedisRepository memberCouponRedisRepository;

    @Scheduled(fixedDelay = 120000)
    @Transactional
    public void updateCouponStock() {
        Map<Long, Long> memberCouponCountMap = memberCouponRedisRepository.getMemberCouponCounts();
        Set<Long> couponIdSet = memberCouponCountMap.keySet();

        List<Coupon> couponList = couponRepository.findAllByIdIn(couponIdSet);
        couponList.forEach(coupon -> {
            Long count = memberCouponCountMap.get(coupon.getId());
            Optional.ofNullable(count).ifPresent(coupon::decreaseCouponStock);
        });

        memberCouponRedisRepository.deleteMemberCouponCount();
    }

}
