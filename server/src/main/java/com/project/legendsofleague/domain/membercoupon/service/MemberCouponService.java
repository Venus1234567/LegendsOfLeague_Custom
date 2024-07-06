package com.project.legendsofleague.domain.membercoupon.service;

import com.project.legendsofleague.common.exception.GlobalExceptionFactory;
import com.project.legendsofleague.common.exception.NotFoundInputValueException;
import com.project.legendsofleague.domain.coupon.domain.Coupon;
import com.project.legendsofleague.domain.coupon.domain.CouponType;
import com.project.legendsofleague.domain.coupon.repository.CouponRepository;
import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.item.domain.ItemCategory;
import com.project.legendsofleague.domain.membercoupon.domain.MemberCoupon;
import com.project.legendsofleague.domain.membercoupon.dto.MemberCouponCreateDto;
import com.project.legendsofleague.domain.membercoupon.dto.MemberCouponResponseDto;
import com.project.legendsofleague.domain.membercoupon.exception.AlreadyRegisteredCouponException;
import com.project.legendsofleague.domain.membercoupon.exception.NotEnoughCouponStockException;
import com.project.legendsofleague.domain.membercoupon.repository.MemberCouponRedisRepository;
import com.project.legendsofleague.domain.membercoupon.repository.MemberCouponRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;
    private final MemberCouponRedisRepository memberCouponRedisRepository;

    /**
     * 등록 가능한 쿠폰을 회원이 쿠폰을 등록하는 메서드.
     *
     * @param memberId
     * @param memberCouponCreateDto
     */
    @Transactional
    public void registerMemberCoupon(Long memberId, MemberCouponCreateDto memberCouponCreateDto) {
        Long couponId = memberCouponCreateDto.getCouponId();
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> {
                throw GlobalExceptionFactory.getInstance(NotFoundInputValueException.class);
            });

        validateAlreadyRegisteredCoupon(coupon);

        Long memberCouponCount = memberCouponRedisRepository.increaseMemberCouponCount(couponId);

        if (memberCouponCount > coupon.getStock()) {
            throw GlobalExceptionFactory.getInstance(NotEnoughCouponStockException.class);
        }

        try{
            memberCouponRepository.save(MemberCoupon.toEntity(memberId, coupon));
        }catch (Exception e){
            memberCouponRedisRepository.decreaseMemberCouponCount(couponId);
            throw e;
        }

    }


    private void validateAlreadyRegisteredCoupon(Coupon coupon) {
        memberCouponRepository.findByCouponId(coupon.getId()).ifPresent(memberCoupon -> {
            throw GlobalExceptionFactory.getInstance(AlreadyRegisteredCouponException.class);
        });
    }

    public List<MemberCouponResponseDto> getMemberCoupons(Long memberId) {
        return memberCouponRepository.queryMemberCoupons(
                memberId).stream()
            .map(MemberCouponResponseDto::new)
            .collect(Collectors.toList());
    }


    public Map<Long, List<MemberCouponResponseDto>> getMemberCouponsByOrder(Long memberId,
        Long orderId,
        List<Item> itemList) {
        Map<Long, List<MemberCouponResponseDto>> couponMap = new HashMap<>();

        Map<Long, List<MemberCoupon>> itemMemberCouponMap = new HashMap<>();

        Map<Long, List<MemberCoupon>> itemCouponMap = new HashMap<>();

        Map<ItemCategory, List<MemberCoupon>> categoryCouponMap = new HashMap<>();

        memberCouponRepository.queryMemberCouponsByOrder(memberId, orderId)
            .forEach(memberCoupon -> {
                Coupon coupon = memberCoupon.getCoupon();

                if (coupon.getCouponType() == CouponType.ITEM_PERCENT_DISCOUNT
                    || coupon.getCouponType() == CouponType.ITEM_AMOUNT_DISCOUNT) {
                    itemCouponMap.computeIfAbsent(coupon.getItem().getId(), k -> new ArrayList<>())
                        .add(memberCoupon);
                }

                if (coupon.getCouponType() == CouponType.CATEGORY_PERCENT_DISCOUNT
                    || coupon.getCouponType() == CouponType.CATEGORY_AMOUNT_DISCOUNT) {
                    categoryCouponMap.computeIfAbsent(coupon.getAppliedCategory(),
                        k -> new ArrayList<>()).add(memberCoupon);
                }
            });

        itemList.forEach(item -> {
            Long itemId = item.getId();
            ItemCategory category = item.getCategory();

            if (itemCouponMap.containsKey(itemId)) {
                itemMemberCouponMap.computeIfAbsent(itemId,
                        k -> new ArrayList<>(itemCouponMap.get(itemId)));

            }

            if (categoryCouponMap.containsKey(category)) {
                itemMemberCouponMap.computeIfAbsent(itemId,
                        k -> new ArrayList<>(categoryCouponMap.get(category)));
            }
        });

        itemMemberCouponMap.forEach((key, value) -> {
            couponMap.put(key, value.stream()
                .map(MemberCouponResponseDto::new)
                .collect(Collectors.toList()));
        });

        return couponMap;
    }


}
