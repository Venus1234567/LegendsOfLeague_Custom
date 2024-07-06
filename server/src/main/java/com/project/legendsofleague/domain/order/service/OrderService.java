package com.project.legendsofleague.domain.order.service;


import com.project.legendsofleague.common.exception.GlobalExceptionFactory;
import com.project.legendsofleague.common.exception.NotFoundInputValueException;
import com.project.legendsofleague.common.exception.WrongInputException;
import com.project.legendsofleague.domain.cartItem.domain.CartItem;
import com.project.legendsofleague.domain.cartItem.dto.CartItemOrderRequestDto;
import com.project.legendsofleague.domain.cartItem.service.CartItemService;
import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.item.repository.ItemRepository;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.membercoupon.service.MemberCouponService;
import com.project.legendsofleague.domain.order.domain.Order;
import com.project.legendsofleague.domain.order.domain.OrderItem;
import com.project.legendsofleague.domain.order.domain.OrderStatus;
import com.project.legendsofleague.domain.order.dto.OrderInfoResponseDto;
import com.project.legendsofleague.domain.order.dto.OrderListResponseDto;
import com.project.legendsofleague.domain.order.dto.OrderRequestDto;
import com.project.legendsofleague.domain.order.dto.OrderResponseDto;
import com.project.legendsofleague.domain.order.repository.order.OrderRepository;
import com.project.legendsofleague.domain.purchase.domain.Purchase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemService orderItemService;
    private final MemberCouponService memberCouponService;
    private final CartItemService cartItemService;


    public List<Purchase> getOrderList(Member member) {
        return orderRepository.queryOrderByMember(member.getId());
    }

    public List<OrderListResponseDto> findOrderList(Member member) {
        List<Purchase> purchase = getOrderList(member);

        return purchase.stream()
            .map(OrderListResponseDto::toDto)
            .toList();
    }

    /**
     * @param orderRequestDto
     * @param member
     * @return
     */
    @Transactional
    public Long createOrder(OrderRequestDto orderRequestDto, Member member) {

        Item item = itemRepository.findById(orderRequestDto.getItemId())
            .orElseThrow(() -> new NotFoundException("유효하지 않은 아이템입니다."));

        OrderItem orderItem = OrderItem.createOrderItem(item, orderRequestDto.getCount());
        Order order = Order.toEntity(member, orderItem);
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 장바구니 목록에서 사용자가 체크한 1개 이상의 아이템에 대한 주문에 대한 주문 객체 생성
     *
     * @return
     */
    @Transactional
    public Long createOrderFromCart(List<CartItemOrderRequestDto> cartItemRequestList,
        Member member) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<CartItem> cartItems = cartItemService.getCartItemList(member.getId());
        List<Long> cartItemIds = cartItems.stream().map(CartItem::getId).toList();

        validateCartList(cartItemIds, cartItemRequestList);

        Map<Long, CartItem> cartItemMap = cartItems.stream()
            .collect(Collectors.toMap(CartItem::getId, c -> c));

        for (CartItemOrderRequestDto cartItemOrderRequestDto : cartItemRequestList) {
            CartItem cartItem = cartItemMap.get(cartItemOrderRequestDto.getCartItemId());
            Item item = cartItem.getItem();
            OrderItem orderItem = OrderItem.createOrderItem(item, cartItem.getCount());
            orderItems.add(orderItem);

        }

        Order order = Order.toEntity(member, orderItems);
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 장바구니를 통해 요청했는데, 장바구니에 item 목록이 없는 요청이 있는 지 확인
     * ex) 장바구니를 통해 item2, item3에 대한 order를 만드려고 하는데, cart에 item2가 없는 지 확인
     *
     * @param cartItemIds
     * @param cartItemRequestList
     */
    private void validateCartList(List<Long> cartItemIds,
        List<CartItemOrderRequestDto> cartItemRequestList) {
        for (CartItemOrderRequestDto cartItemRequestDto : cartItemRequestList) {
            if (!cartItemIds.contains(cartItemRequestDto.getCartItemId())) {
                throw new RuntimeException("옳지 않은 요청입니다.");
            }
        }
    }

    /**
     * 사용자가 상세 아이템에서 주문하기 버튼을 클릭했을 때 보여지는 화면을 위한 서비스 메소드
     *
     * @param memberId (현재 로그인한 유저 정보)
     * @param orderId
     * @return
     */
    public OrderResponseDto detailOrderPage(Member member, Long orderId) {

        List<OrderItem> orderItems = orderItemService.getOrderItemList(orderId);

        if (orderItems.isEmpty()) {
            throw new NotFoundException("유효하지 않은 주문입니다.");
        }

        Member OrderMember = orderItems.get(0).getOrder().getMember();

        if (!OrderMember.getId().equals(member.getId())) {
            throw new RuntimeException("허용되지 않은 접근입니다.");
        }

        List<Item> items = orderItems.stream()
            .map(OrderItem::getItem).toList();

        return OrderResponseDto.toDto(orderItems,
            memberCouponService.getMemberCouponsByOrder(member.getId(), orderId, items));
    }

    @Transactional
    public void refundOrder(Member member, Order order) {

        //유효하지 않은 order를 요청한 거였다면 (이미 삭제된 order라면)
        if (order.getOrderItemList().isEmpty()) {
            throw new RuntimeException("유효하지 않은 요청입니다.");
        }

        if (!order.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("유효하지 않은 요청입니다.");
        }

        if (order.getOrderStatus() != OrderStatus.SUCCESS) {
            throw new RuntimeException("유효하지 않은 요청입니다.");
        }
    }

    @Transactional
    public boolean successPurchase(LocalDateTime time, Long orderId, Integer totalPrice) {
        List<OrderItem> orderItems = orderItemService.getOrderItemList(orderId);

        if (orderItems.isEmpty()) {
            return false;
        }

        Order order = orderItems.get(0).getOrder();

        //아이템 제고에 문제가 없다면 order의 상태를 SUCCESS로 변경, 결제 완료된 총 totalPrice 초기화, orderDate를 결제 완료 시간으로 변경
        order.changeStatusToSuccess(time, totalPrice);
        //주문 내역 중 장바구니에 있던 아이템이 있다면 장바구니에서 해당 아이템 삭제하기
        cartItemService.deleteOrderedCartItem(order.getMember(), orderItems);

        return true;
    }




    private boolean checkItemStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            Integer count = orderItem.getCount();
            if (item.getStock() - count < 0) {
                return false;
            }
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            Integer count = orderItem.getCount();
            item.removeStock(count);
        }
    }

    public OrderInfoResponseDto getOrderInfoPage(Member member, Long orderId) {

        Purchase purchase = orderRepository.queryOrderByOrderId(orderId).orElseThrow(() -> {
            throw GlobalExceptionFactory.getInstance(NotFoundInputValueException.class);
        });

        Order order = purchase.getOrder();
        if(order.getMember().getId() != member.getId()){
            throw GlobalExceptionFactory.getInstance(WrongInputException.class);
        }

        return OrderInfoResponseDto.from(purchase);
    }
}
