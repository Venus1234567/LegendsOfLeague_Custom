package com.project.legendsofleague.domain.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.legendsofleague.common.exception.GlobalExceptionFactory;
import com.project.legendsofleague.common.exception.NotFoundInputValueException;
import com.project.legendsofleague.common.exception.WrongInputException;
import com.project.legendsofleague.domain.coupon.service.CouponService;
import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.item.repository.ItemRepository;
import com.project.legendsofleague.domain.item.service.ItemStockFacade;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.membercoupon.domain.MemberCoupon;
import com.project.legendsofleague.domain.membercoupon.repository.MemberCouponRepository;
import com.project.legendsofleague.domain.order.domain.Order;
import com.project.legendsofleague.domain.order.dto.OrderItemStockDto;
import com.project.legendsofleague.domain.order.repository.order.OrderRepository;
import com.project.legendsofleague.domain.order.service.OrderService;
import com.project.legendsofleague.domain.purchase.domain.Purchase;
import com.project.legendsofleague.domain.purchase.domain.PurchaseType;
import com.project.legendsofleague.domain.purchase.dto.ItemCouponAppliedDto;
import com.project.legendsofleague.domain.purchase.dto.PurchaseResponseDto;
import com.project.legendsofleague.domain.purchase.dto.PurchaseStartRequestDto;
import com.project.legendsofleague.domain.purchase.dto.PurchaseSuccessResponseDto;
import com.project.legendsofleague.domain.purchase.exception.InvalidMemberCouponException;
import com.project.legendsofleague.domain.purchase.exception.WrongPriceException;
import com.project.legendsofleague.domain.purchase.repository.PurchaseRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeforePurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ItemRepository itemRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final OrderRepository orderRepository;

    private final OrderService orderService;
    private final ItemStockFacade itemStockFacade;
    private final KakaoService kakaoService;
    private final TossService tossService;
    private final CouponService couponService;
    private final AfterPurchaseService afterPurchaseService;

    /*
    결제 시작 전 체크 로직
     */
    @Transactional
    public PurchaseResponseDto startPurchase(Member member,
        PurchaseStartRequestDto purchaseStartRequestDto) {

        Long orderId = purchaseStartRequestDto.getOrderId();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            throw GlobalExceptionFactory.getInstance(NotFoundInputValueException.class);
        });
        String orderCode = order.getOrderCode();

        List<ItemCouponAppliedDto> itemList = purchaseStartRequestDto.getItemList();
        Integer purchaseTotalPrice = purchaseStartRequestDto.getPurchaseTotalPrice();
        PurchaseType purchaseType = PurchaseType.valueOf(purchaseStartRequestDto.getProvider());

        Map<Long, Item> itemMap = itemRepository.findAllById(
                itemList.stream().map(ItemCouponAppliedDto::getItemId)
                    .collect(Collectors.toList()))
            .stream()
            .collect(Collectors.toMap(Item::getId, item -> item));

        Map<Long, MemberCoupon> memberCouponMap = getMemberCouponMap(member.getId(), itemList);

        if (!couponService.checkValidity(memberCouponMap, itemList, itemMap)) {
            throw GlobalExceptionFactory.getInstance(InvalidMemberCouponException.class);
        }

        checkTotalPrice(itemList, purchaseTotalPrice);



        //결제 이름 생성
        int quantity = itemList.stream().mapToInt(ItemCouponAppliedDto::getQuantity).sum();
        String orderName = makeOrderName(
            itemList.stream().map(ItemCouponAppliedDto::getItemName).toList(), quantity);

        //결제 엔티티 생성
        Purchase createdPurchase
            = Purchase.toEntity(quantity, orderName, purchaseTotalPrice, purchaseType,
            order);
        purchaseRepository.save(createdPurchase);

        //재고 차감
        List<OrderItemStockDto> orderItemStockDtoList = itemList.stream()
            .map(itemCouponAppliedDto -> new OrderItemStockDto(
                itemMap.get(itemCouponAppliedDto.getItemId()),
                itemCouponAppliedDto.getQuantity()))
            .collect(Collectors.toList());

        itemStockFacade.decreaseStock(orderItemStockDtoList);

        //쿠폰과 결제 연결하기
        for (MemberCoupon memberCoupon : memberCouponMap.values()) {
            memberCoupon.updatePurchase(createdPurchase);
        }

        return new PurchaseResponseDto(createdPurchase.getId(),
            purchaseTotalPrice, purchaseType.name(), orderName, orderCode, member.getNickname());
    }



    public void cancelPurchase(Member member, Long orderId) throws JsonProcessingException {

        Purchase purchase = purchaseRepository.queryPurchaseByOrderId(orderId)
            .orElseThrow(
                () -> GlobalExceptionFactory.getInstance(NotFoundInputValueException.class));

        Order order = purchase.getOrder();

        orderService.refundOrder(member, order);

        PurchaseType purchaseType = purchase.getPurchaseType();
        if (purchaseType == PurchaseType.KAKAO) {
            kakaoService.cancelPurchase(purchase);
        } else if (purchaseType == PurchaseType.TOSS) {
            tossService.cancelPurchase(purchase);
        }

        afterPurchaseService.refundPurchase(purchase);
    }

    @Transactional(readOnly = true)
    public PurchaseSuccessResponseDto queryPurchaseInfo(Long purchaseId) {
        Purchase purchase = purchaseRepository.queryPurchase(purchaseId).orElseThrow(() ->
            GlobalExceptionFactory.getInstance(NotFoundInputValueException.class)
        );

        return PurchaseSuccessResponseDto.from(purchase);
    }


    /**
     * 입력받은 MemberCoupon의  id 전체를 가지고 쿼리문을 날리고 하나의 Map으로 만드는 메서드.
     *
     * @param memberId
     * @param itemList
     * @return <memberCouponId, MemberCoupon>
     */
    private Map<Long, MemberCoupon> getMemberCouponMap(Long memberId,
        List<ItemCouponAppliedDto> itemList) {

        List<Long> memberCouponIdList = itemList.stream()
            .map(ItemCouponAppliedDto::getMemberCouponId)
            .filter(Objects::nonNull)
            .toList();

        Map<Long, MemberCoupon> memberCouponMap = memberCouponRepository.queryMemberCouponsByIdList(
            memberId,
            memberCouponIdList);

        if (memberCouponMap.size() != memberCouponIdList.size()) {
            throw GlobalExceptionFactory.getInstance(InvalidMemberCouponException.class);
        }

        return memberCouponMap;
    }

    /**
     * 입력받은 각 아이템에 최종 가격의 합과 구매 전체 가격이 같은지 검증
     *
     * @param itemList
     * @param purchaseTotalPrice
     */
    private void checkTotalPrice(List<ItemCouponAppliedDto> itemList, Integer purchaseTotalPrice) {
        int totalPrice = itemList.stream()
            .mapToInt(ItemCouponAppliedDto::getPrice)
            .sum();
        if (totalPrice != purchaseTotalPrice) {
            throw GlobalExceptionFactory.getInstance(WrongPriceException.class);
        }
    }


    /**
     * 주문 이름을 만들어주는 메서드
     *
     * @param itemNameList
     * @param quantity
     * @return
     */
    private String makeOrderName(List<String> itemNameList, Integer quantity) {
        if (itemNameList.isEmpty()) {
            throw GlobalExceptionFactory.getInstance(WrongInputException.class);
        } else {
            String exampleItemName = itemNameList.get(0);

            return exampleItemName + " 외 " + (quantity - 1) + "건";
        }
    }


}
