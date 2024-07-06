package com.project.legendsofleague.domain.purchase.service;

import com.project.legendsofleague.common.exception.GlobalExceptionFactory;
import com.project.legendsofleague.common.exception.NotFoundInputValueException;
import com.project.legendsofleague.domain.item.service.ItemStockFacade;
import com.project.legendsofleague.domain.membercoupon.domain.MemberCoupon;
import com.project.legendsofleague.domain.order.domain.Order;
import com.project.legendsofleague.domain.order.domain.OrderItem;
import com.project.legendsofleague.domain.order.dto.OrderItemStockDto;
import com.project.legendsofleague.domain.order.service.OrderService;
import com.project.legendsofleague.domain.purchase.domain.Purchase;
import com.project.legendsofleague.domain.purchase.domain.PurchaseStatus;
import com.project.legendsofleague.domain.purchase.repository.PurchaseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AfterPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ItemStockFacade itemStockFacade;
    private final OrderService orderService;


    @Transactional
    public Boolean finishPurchase(Long purchaseId, String code) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElseThrow(
            () -> GlobalExceptionFactory.getInstance(NotFoundInputValueException.class)
        );

        purchase.updatePurchaseCode(code);

        //사용한 쿠폰 처리
        LocalDate usedDate = LocalDate.now();
        purchase.getMemberCouponList().forEach(
            memberCoupon -> memberCoupon.updatedUsedHistory(usedDate)
        );

        //OrderDate, OrderId, TotalPrice를 orderservice의 특정 메서드로 넘기기
        return orderService.successPurchase(LocalDateTime.now(), purchase.getOrder().getId(),
            purchase.getTotalPrice());
    }


    @Transactional
    public void refundPurchase(Purchase purchase) {
        purchase.updatePurchaseStatus(PurchaseStatus.REFUND);

        refundMemberCoupon(purchase);

        purchase.getOrder().refundOrder();

        List<OrderItem> orderItemList = purchase.getOrder().getOrderItemList();
        itemStockFacade.increaseStock(
            toOrderItemStockDtoList(orderItemList)
        );
    }


    @Transactional
    public void handleFailPurchase(Purchase purchase) {
        Order order = purchase.getOrder();

        itemStockFacade.increaseStock(
            toOrderItemStockDtoList(order.getOrderItemList())
        );

        purchase.updatePurchaseStatus(PurchaseStatus.CANCEL);

        order.changeStatusToCancel();
    }

    private void refundMemberCoupon(Purchase purchase) {
        purchase.getMemberCouponList().forEach(MemberCoupon::revertUsedHistory);
    }

    private List<OrderItemStockDto> toOrderItemStockDtoList(List<OrderItem> orderItemList) {
        return orderItemList.stream()
            .map(OrderItemStockDto::new)
            .toList();
    }
}
