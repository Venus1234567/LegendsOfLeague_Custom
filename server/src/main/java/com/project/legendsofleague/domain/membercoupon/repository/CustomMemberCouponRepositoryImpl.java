package com.project.legendsofleague.domain.membercoupon.repository;

import static com.project.legendsofleague.domain.coupon.domain.QCoupon.coupon;
import static com.project.legendsofleague.domain.item.domain.QItem.item;
import static com.project.legendsofleague.domain.member.domain.QMember.member;
import static com.project.legendsofleague.domain.membercoupon.domain.QMemberCoupon.memberCoupon;
import static com.project.legendsofleague.domain.order.domain.QOrderItem.orderItem;
import static com.querydsl.core.group.GroupBy.groupBy;

import com.project.legendsofleague.domain.membercoupon.domain.MemberCoupon;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomMemberCouponRepositoryImpl implements CustomMemberCouponRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberCoupon> queryMemberCoupons(Long memberId) {
        return queryFactory.selectFrom(memberCoupon)
            .join(memberCoupon.coupon, coupon).fetchJoin()
            .where(memberCoupon.member.id.eq(memberId))
            .where(checkValidity())
            .fetch();
    }

    @Override
    public List<MemberCoupon> queryMemberCouponsByOrder(Long memberId, Long orderId) {
        return queryFactory.selectFrom(memberCoupon).distinct()
            .leftJoin(memberCoupon.coupon, coupon).fetchJoin()
            .leftJoin(coupon.item, item).fetchJoin()
            .where(memberCoupon.member.id.eq(memberId))
            .where(checkValidity())
            .where(memberCoupon.coupon.item.in(
                JPAExpressions
                    .select(orderItem.item)
                    .from(orderItem)
                    .where(orderItem.order.id.eq(orderId))
            ).or(
                memberCoupon.coupon.appliedCategory.in(
                    JPAExpressions
                        .select(orderItem.item.category)
                        .from(orderItem)
                        .where(orderItem.order.id.eq(orderId))
                )
            ))
            .fetch();
    }

    @Override
    public Map<Long, MemberCoupon> queryMemberCouponsByIdList(Long memberId,
        List<Long> memberCouponIdList) {
        return queryFactory.selectFrom(memberCoupon)
            .leftJoin(memberCoupon.member, member).fetchJoin()
            .leftJoin(memberCoupon.coupon, coupon).fetchJoin()
            .leftJoin(coupon.item, item).fetchJoin()
            .where(memberCoupon.member.id.eq(memberId))
            .where(memberCoupon.id.in(memberCouponIdList))
            .transform(groupBy(memberCoupon.id).as(memberCoupon));
    }

    private BooleanExpression checkValidity() {
        LocalDate now = LocalDate.now();

        return memberCoupon.coupon.validityStartDate.loe(now)
            .and(memberCoupon.coupon.validityEndDate.goe(now))
            .and(memberCoupon.isUsed.isFalse());
    }
}
