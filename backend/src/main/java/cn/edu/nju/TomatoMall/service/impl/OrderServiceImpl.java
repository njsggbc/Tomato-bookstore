package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.enums.OrderEvent;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.dto.shipment.*;
import cn.edu.nju.TomatoMall.models.po.*;
import cn.edu.nju.TomatoMall.repository.*;
import cn.edu.nju.TomatoMall.service.InventoryService;
import cn.edu.nju.TomatoMall.service.OrderService;
import cn.edu.nju.TomatoMall.service.impl.events.order.OrderCancelEvent;
import cn.edu.nju.TomatoMall.service.impl.events.order.OrderConfirmEvent;
import cn.edu.nju.TomatoMall.service.impl.events.order.OrderDeliverEvent;
import cn.edu.nju.TomatoMall.service.impl.events.order.OrderShipEvent;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 提供订单管理、购物车操作以及客户、卖家和管理员的订单处理功能
 */
@Service
public class OrderServiceImpl implements OrderService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtil securityUtil;
    private final StoreRepository storeRepository;
    private final EmploymentRepository employmentRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;
    private final ShippingInfoRepository shippingInfoRepository;

    @Autowired
    public OrderServiceImpl(ProductRepository productRepository,
                            CartItemRepository cartItemRepository,
                            OrderRepository orderRepository,
                            SecurityUtil securityUtil,
                            StoreRepository storeRepository,
                            EmploymentRepository employmentRepository,
                            InventoryService inventoryService,
                            ApplicationEventPublisher eventPublisher,
                            PaymentRepository paymentRepository,
                            ShippingInfoRepository shippingInfoRepository
    ) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.securityUtil = securityUtil;
        this.storeRepository = storeRepository;
        this.employmentRepository = employmentRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
        this.paymentRepository = paymentRepository;
        this.shippingInfoRepository = shippingInfoRepository;
    }

    //---------------------------
    // 客户相关方法
    //---------------------------

    /**
     * 处理多个购物车项的结算
     * 按店铺分组商品并检查每个商品的库存状态
     *
     * @param cartItemIds 购物车项ID列表
     * @return 结算响应列表，包含每个购物车项的ID和是否有足够库存的状态
     * @throws TomatoMallException 当购物车项无效时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public List<CheckoutResponse> checkout(List<Integer> cartItemIds) {
        List<CartItem> cartItems = getValidCartItems(securityUtil.getCurrentUser(), cartItemIds);
        if (cartItems.isEmpty()) {
            throw TomatoMallException.invalidCartItem();
        }
        return cartItems.stream()
                .map(item -> {
                    int availableStock = inventoryService.getAvailableStock(item.getProduct().getId());
                    return new CheckoutResponse(item.getId(), availableStock >= item.getQuantity());
                })
                .collect(Collectors.toList());
    }

    /**
     * 提交订单
     * 创建订单并初始化支付
     *
     * @param cartItemIds 购物车项ID列表
     * @param recipientName 收货人姓名
     * @param recipientPhone 收货人电话
     * @param recipientAddress 收货地址
     * @param storeRemarks 店铺备注
     * @return 支付信息响应
     * @throws TomatoMallException 当购物车项无效时抛出异常
     */
    @Override
    @Transactional
    public PaymentInfoResponse submit(
            List<Integer> cartItemIds,
            String recipientName,
            String recipientPhone,
            String recipientAddress,
            Map<Integer, String> storeRemarks
    ) {
        User user = securityUtil.getCurrentUser();

        // 获取并验证购物车项
        List<CartItem> cartItems = getValidCartItems(user, cartItemIds);
        if (cartItems.isEmpty()) {
            throw TomatoMallException.invalidCartItem();
        }

        // 按店铺分组商品并创建订单
        List<Order> orders = groupByStore(cartItems).entrySet().stream()
                .map(entry -> buildOrder(
                        user,
                        entry.getKey(),
                        entry.getValue(),
                        recipientAddress,
                        recipientPhone,
                        recipientName,
                        storeRemarks == null ? "" : storeRemarks.get(entry.getKey().getId())
                ))
                .collect(Collectors.toList());

        // 为所有订单创建单一支付
        Payment payment = Payment.builder()
                .user(user)
                .orders(orders)
                .build();

        paymentRepository.save(payment);

        return new PaymentInfoResponse(paymentRepository.findById(payment.getId()).orElseThrow(TomatoMallException::unexpectedError));
    }

    /**
     * 获取当前用户的购物车项列表
     *
     * @return 购物车项信息响应列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CartItemInfoResponse> getCartItemList(int page, int size, String field, boolean order) {
        User user = securityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));
        return cartItemRepository.findAllByUserId(user.getId(), pageable)
                .map(CartItemInfoResponse::new);
    }

    /**
     * 添加商品到购物车
     *
     * @param productId 商品ID
     * @param quantity 数量
     * @return 购物车项ID
     * @throws TomatoMallException 当操作无效时抛出异常
     */
    @Override
    @Transactional
    public int addToCart(int productId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(securityUtil.getCurrentUser().getId(), productId)
                .orElse(null);
        if (item != null) {
            // 如果购物车中已存在该商品，则更新数量和时间戳
            item.setQuantity(item.getQuantity() + quantity);
            item.setTimestamp(LocalDateTime.now());
            cartItemRepository.save(item);
            return item.getId();
        }

        // 如果购物车中不存在该商品，则创建新的购物车项
        return cartItemRepository.save(
                CartItem.builder()
                        .product(productRepository.findById(productId)
                                .orElseThrow(TomatoMallException::productNotFound))
                        .user(securityUtil.getCurrentUser())
                        .quantity(quantity)
                        .build()
        ).getId();
    }

    /**
     * 从购物车中移除商品
     *
     * @param cartItemId 购物车项ID
     * @throws TomatoMallException 当操作无效时抛出异常
     */
    @Override
    @Transactional
    public void removeFromCart(int cartItemId) {
        try {
            cartItemRepository.deleteByIdAndUserId(cartItemId, securityUtil.getCurrentUser().getId());
        } catch (Exception e) {
            throw TomatoMallException.invalidOperation();
        }
    }

    /**
     * 更新购物车中商品的数量
     *
     * @param cartItemId 购物车项ID
     * @param quantity 更新的数量
     * @throws TomatoMallException 当操作无效时抛出异常
     */
    @Override
    @Transactional
    public void updateCartItemQuantity(int cartItemId, int quantity) {
        try {
            cartItemRepository.updateCartItemQuantityByIdAndUserId(cartItemId, securityUtil.getCurrentUser().getId(), quantity);
        } catch (Exception e) {
            throw TomatoMallException.invalidOperation();
        }
    }

    /**
     * 获取订单详细信息
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 客户订单详细信息响应
     * @throws TomatoMallException 当订单未找到时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerOrderInfoResponse getOrderInfo(Integer orderId, String orderNo) {
        if (orderId != null) {
            return new CustomerOrderInfoResponse(
                    orderRepository.findByIdAndUserId(orderId, securityUtil.getCurrentUser().getId())
                            .orElseThrow(TomatoMallException::orderNotFound)
            );
        } else if (orderNo != null) {
            return new CustomerOrderInfoResponse(
                    orderRepository.findByOrderNoAndUserId(orderNo, securityUtil.getCurrentUser().getId())
                            .orElseThrow(TomatoMallException::orderNotFound)
            );
        } else {
            throw TomatoMallException.orderNotFound();
        }
    }

    /**
     * 获取客户订单列表，按状态过滤
     *
     * @param status 请求的订单状态
     * @return 订单简要信息响应列表
     * @throws TomatoMallException 当操作无效时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderBriefResponse> getOrderList(int page, int size, String field, boolean order, CustomerRequestOrderStatus status) {
        List<OrderStatus> statusList;
        switch (status) {
            case ALL:
                statusList = Arrays.asList(OrderStatus.values());
                break;
            case AWAITING_PAYMENT:
                statusList = Collections.singletonList(OrderStatus.AWAITING_PAYMENT);
                break;
            case AWAITING_SHIPMENT:
                statusList = Arrays.asList(OrderStatus.PROCESSING, OrderStatus.AWAITING_SHIPMENT);
                break;
            case AWAITING_RECEIPT:
                statusList = Arrays.asList(OrderStatus.IN_TRANSIT, OrderStatus.AWAITING_RECEIPT);
                break;
            case COMPLETED:
                statusList = Arrays.asList(OrderStatus.COMPLETED, OrderStatus.CLOSED);
                break;
            case AFTER_SALE:
                statusList = OrderStatus.afterSaleStatus();
                break;
            default:
                throw TomatoMallException.invalidOperation();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        return orderRepository.findAllByUserIdAndStatusIn(securityUtil.getCurrentUser().getId(), statusList, pageable)
                .map(OrderBriefResponse::new);
    }

    /**
     * 取消订单（前端接口调用方法）
     * 释放库存并申请退款
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     * @throws TomatoMallException 当订单未找到或状态不允许取消时抛出异常
     */
    @Override
    @Transactional
    public void cancel(int orderId, String reason) {
        Order order = orderRepository.findByIdAndUserId(orderId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::orderNotFound);
        cancel(order, reason);
    }

    /**
     * 内部取消订单方法
     * 仅供其他服务调用
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     */
    @Override
    @Transactional
    public void cancelInternal(int orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(TomatoMallException::orderNotFound);
        cancel(order, reason);
    }

    private void cancel(Order order, String reason) {
        switch (order.getStatus()) {
            case CANCELLED:
                return;
            case AWAITING_PAYMENT:
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case PROCESSING:
            case AWAITING_SHIPMENT:
                order.getItems().forEach(item -> inventoryService.unlockStock(item.getProductId(), item.getQuantity()));
                order.setStatus(OrderStatus.REFUND_PROCESSING);
                break;
            default:
                throw TomatoMallException.invalidOperation();
        }

        order.getLogs().add(OrderLog.builder()
                .operator(securityUtil.getCurrentUser())
                .order(order)
                .event(OrderEvent.CANCEL)
                .afterEventStatus(order.getStatus())
                .message("用户取消订单: " + reason)
                .timestamp(LocalDateTime.now())
                .build());

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCancelEvent(order, order.getTotalAmount(),"用户取消订单: " + reason));
    }

    /**
     * 确认收货
     * 将订单状态更新为已完成
     *
     * @param orderId 订单ID
     * @throws TomatoMallException 当订单未找到或状态不允许确认收货时抛出异常
     */
    @Override
    @Transactional
    public void confirmReceipt(int orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.AWAITING_RECEIPT) {
            throw TomatoMallException.invalidOperation();
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.getLogs().add(OrderLog.builder()
                .operator(securityUtil.getCurrentUser())
                .order(order)
                .event(OrderEvent.CONFIRM_RECEIPT)
                .afterEventStatus(OrderStatus.COMPLETED)
                .message("确认收货")
                .timestamp(LocalDateTime.now())
                .build());

        orderRepository.save(order);
    }

    //-----------------------------
    // 商家相关方法
    //-----------------------------

    /**
     * 获取店铺订单列表，按状态过滤
     *
     * @param storeId 店铺ID
     * @param status 请求的订单状态
     * @return 订单简要信息响应列表
     * @throws TomatoMallException 当用户无操作该店铺的权限时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderBriefResponse> getStoreOrderList(int storeId, int page, int size, String field, boolean order, StoreRequestOrderStatus status) {
        validateStorePermission(storeId);

        List<OrderStatus> statusList;
        switch (status) {
            case ALL:
                statusList = Arrays.asList(OrderStatus.values());
                break;
            case AWAITING_PROCESSING:
                statusList = Collections.singletonList(OrderStatus.PROCESSING);
                break;
            case AWAITING_SHIPMENT:
                statusList = Collections.singletonList(OrderStatus.AWAITING_SHIPMENT);
                break;
            case AWAITING_TRANSACTION:
                statusList = Arrays.asList(OrderStatus.IN_TRANSIT, OrderStatus.AWAITING_RECEIPT);
                break;
            case COMPLETED:
                statusList = Arrays.asList(OrderStatus.COMPLETED, OrderStatus.CLOSED);
                break;
            case AFTER_SALE:
                statusList = OrderStatus.afterSaleStatus();
                break;
            default:
                throw TomatoMallException.invalidOperation();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        return orderRepository.findAllByStoreIdAndStatusIn(storeId, statusList, pageable)
                .map(OrderBriefResponse::new);
    }

    /**
     * 获取店铺订单详细信息
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 店铺订单详细信息响应
     * @throws TomatoMallException 当用户无操作该店铺的权限或订单未找到时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public StoreOrderInfoResponse getStoreOrderInfo(int storeId, Integer orderId, String orderNo) {
        validateStorePermission(storeId);

        if (orderId != null) {
            return new StoreOrderInfoResponse(
                    orderRepository.findByIdAndStoreId(orderId, storeId)
                            .orElseThrow(TomatoMallException::orderNotFound)
            );
        } else if (orderNo != null) {
            return new StoreOrderInfoResponse(
                    orderRepository.findByOrderNoAndStoreId(orderNo, storeId)
                            .orElseThrow(TomatoMallException::orderNotFound)
            );
        } else {
            throw TomatoMallException.orderNotFound();
        }
    }

    /**
     * 商家确认订单
     * 确认库存扣减并更新订单状态
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @throws TomatoMallException 当用户无操作该店铺的权限、订单未找到或状态不允许确认时抛出异常
     */
    @Override
    @Transactional
    public void confirm(int storeId, int orderId) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw TomatoMallException.invalidOperation();
        }

        order.getItems().forEach(item -> {
            inventoryService.confirmStockDeduction(item.getProductId(), item.getQuantity());
        });
        order.setStatus(OrderStatus.AWAITING_SHIPMENT);
        order.getLogs().add(OrderLog.builder()
                .operator(securityUtil.getCurrentUser())
                .order(order)
                .event(OrderEvent.CONFIRM)
                .afterEventStatus(OrderStatus.AWAITING_SHIPMENT)
                .message("订单已确认")
                .timestamp(LocalDateTime.now())
                .build());

        // 发布订单确认事件
        eventPublisher.publishEvent(new OrderConfirmEvent(order));

        orderRepository.save(order);
    }

    /**
     * 商家拒绝订单
     * 释放库存并申请退款
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param message 拒绝原因
     * @throws TomatoMallException 当用户无操作该店铺的权限、订单未找到或状态不允许拒绝时抛出异常
     */
    @Override
    @Transactional
    public void refuse(int storeId, int orderId, String message) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw TomatoMallException.invalidOperation();
        }

        order.getItems().forEach(item -> inventoryService.unlockStock(item.getProductId(), item.getQuantity()));
        order.setStatus(OrderStatus.REFUND_PROCESSING);
        order.getLogs().add(OrderLog.builder()
                .operator(securityUtil.getCurrentUser())
                .order(order)
                .event(OrderEvent.REFUSE)
                .afterEventStatus(OrderStatus.REFUND_PROCESSING)
                .message("商家取消订单: " + message)
                .timestamp(LocalDateTime.now())
                .build());

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCancelEvent(order, order.getTotalAmount(),"商家取消订单" + message));
    }

    /**
     * 商家发货
     * 更新商品销量并更新订单状态
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param params 发货请求参数
     * @throws TomatoMallException 当用户无操作该店铺的权限、订单未找到或状态不允许发货时抛出异常
     */
    @Override
    @Transactional
    public void ship(int storeId, int orderId, ShipRequest params) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);
        if (order.getStatus() != OrderStatus.AWAITING_SHIPMENT) {
            throw TomatoMallException.invalidOperation();
        }

        // TODO: 物流发货逻辑，验证并关联订单

        ShippingInfo shippingInfo = order.getShippingInfos().get(0);
        shippingInfo.setShippingCompany(params.getShippingCompany());
        shippingInfo.setTrackingNumber(params.getTrackingNo());

        String senderAddress = (params.getSenderAddress() == null || params.getSenderAddress().isEmpty()) ?
                storeRepository.findAddressById(storeId) : params.getSenderAddress();
        String senderName = (params.getSenderName() == null || params.getSenderName().isEmpty()) ?
                securityUtil.getCurrentUser().getName() : params.getSenderName();
        String senderPhone = (params.getSenderPhone() == null || params.getSenderPhone().isEmpty()) ?
                securityUtil.getCurrentUser().getPhone() : params.getSenderPhone();


        shippingInfo.getLogs().put(LocalDateTime.now(), "已发货," +
                                                        "\n发货地址: " + senderAddress +
                                                        "\n发货人: " + senderName +
                                                        "\n联系电话 " + senderPhone
                );

        shippingInfoRepository.save(shippingInfo);

        // 更新销量
        order.getItems().forEach(item -> productRepository.increaseSalesById(item.getProductId(), item.getQuantity()));

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.getLogs().add(OrderLog.builder()
                .operator(securityUtil.getCurrentUser())
                .order(order)
                .event(OrderEvent.SHIP)
                .afterEventStatus(OrderStatus.IN_TRANSIT)
                .message("已发货," +
                        "\n物流公司: " + params.getShippingCompany().toString() +
                        "\n物流单号: " + params.getTrackingNo()
                        )
                .timestamp(LocalDateTime.now())
                .build());

        orderRepository.save(order);

        // 发布订单发货事件
        eventPublisher.publishEvent(new OrderShipEvent(order, params.getTrackingNo(), params.getShippingCompany().toString()));
    }

    //-----------------------------
    // 管理员相关方法
    //-----------------------------

    /**
     * 终止订单（管理员功能）
     *
     * @param orderId 订单ID
     *
     */
    @Override
    @Transactional
    public void terminate(int orderId) {
        // TODO: 实现管理员终止订单功能
    }

    //-----------------------------
    // 物流相关方法
    //-----------------------------

    @Override
    public void updateShippingInfo(String trackingNo, ShippingUpdateRequest params) {
        ShippingInfo shippingInfo = shippingInfoRepository.findByTrackingNumber(trackingNo)
                .orElseThrow(TomatoMallException::shipmentRecordNotFound);

        // TODO: 更新物流信息逻辑

        shippingInfoRepository.save(shippingInfo);
    }

    @Override
    public void confirmDelivery(String trackingNo, DeliveryConfirmRequest params) {
        ShippingInfo shippingInfo = shippingInfoRepository.findByTrackingNumber(trackingNo)
                .orElseThrow(TomatoMallException::shipmentRecordNotFound);

        // TODO: 更新物流信息

        // 更新订单状态
        Order order = shippingInfo.getOrder();
        order.setStatus(OrderStatus.AWAITING_RECEIPT);
        order.getLogs().add(
                OrderLog.builder()
                        .order(order)
                        .event(OrderEvent.DELIVER)
                        .afterEventStatus(OrderStatus.AWAITING_RECEIPT)
                        .message("已送达: " + params.getDeliveryLocation() + "\n" +
                                "负责人: " + params.getSignedBy() + "\n" +
                                "联系电话: " + params.getPhone() + "\n" +
                                "备注: " + params.getRemark())
                        .timestamp(params.getDeliveryTime())
                        .build()
        );
        orderRepository.save(order);

        // 发布送达事件
        eventPublisher.publishEvent(new OrderDeliverEvent(order, params.getDeliveryTime().toString(), params.getDeliveryLocation()));
    }

    // -----------------------------
    // 公共辅助方法
    //-----------------------------

    @Override
    public void updateStatus(Order order, User operator, OrderEvent event, OrderStatus status, String message) {
        order.setStatus(status);
        order.getLogs().add(
                OrderLog.builder()
                        .order(order)
                        .operator(operator)
                        .event(event)
                        .afterEventStatus(status)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    //-----------------------------
    // 私有辅助方法
    //-----------------------------

    /**
     * 构建订单（完整版本，指定店铺和商品列表）
     *
     * @param user 用户
     * @param store 店铺
     * @param cartItems 购物车项列表
     * @param address 收货地址
     * @param name 收货人姓名
     * @param phone 收货人电话
     * @param remark 订单备注
     * @return 构建的订单对象
     */
    private Order buildOrder(User user,
                             Store store,
                             List<CartItem> cartItems,
                             String address,
                             String name,
                             String phone,
                             String remark
    ) {
        Order order = Order.builder()
                .user(user)
                .store(store)
                .items(cartItems.stream()
                        .map(this::buildOrderItem)
                        .collect(Collectors.toList()))
                .remark(remark)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        // 添加收货信息
        order.getShippingInfos().add(ShippingInfo.builder()
                .order(order)
                .recipientName(getRecipientName(name, user))
                .recipientPhone(getRecipientPhone(phone, user))
                .deliveryAddress(getDeliveryAddress(address, user))
                .build());

        // 添加订单日志
        order.getLogs().add(OrderLog.builder()
                .operator(user)
                .order(order)
                .event(OrderEvent.CREATE)
                .afterEventStatus(OrderStatus.AWAITING_PAYMENT)
                .message("下单成功")
                .timestamp(order.getCreateTime())
                .build());

        return order;
    }

    /**
     * 构建订单项
     * 锁定库存并从购物车中删除相应的购物车项
     *
     * @param cartItem 购物车项
     * @return 构建的订单项对象
     */
    private OrderItem buildOrderItem(CartItem cartItem) {
        int productId = cartItem.getProduct().getId();
        int quantity = cartItem.getQuantity();
        // 锁定库存
        inventoryService.lockStock(productId, quantity);
        // 删除购物车项
        cartItemRepository.delete(cartItem);
        return OrderItem.builder()
                .productId(productId)
                .productSnapshot(cartItem.getProduct().getSnapshot())
                .quantity(quantity)
                .build();
    }

    /**
     * 获取有效的购物车项
     *
     * @param user 用户
     * @param cartItemIds 购物车项ID列表
     * @return 有效的购物车项列表
     * @throws TomatoMallException 当购物车项无效时抛出异常
     */
    private List<CartItem> getValidCartItems(User user, List<Integer> cartItemIds) {
        List<CartItem> items = cartItemRepository.findByIdsAndUserId(cartItemIds, user.getId());
        if (items.size() != cartItemIds.size()) {
            throw TomatoMallException.invalidCartItem();
        }
        return items;
    }

    /**
     * 将购物车项按店铺分组
     *
     * @param items 购物车项列表
     * @return 按店铺分组的购物车项映射
     */
    private Map<Store, List<CartItem>> groupByStore(List<CartItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getStore(),
                        Collectors.toList()
                ));
    }

    /**
     * 获取有效的收货地址
     *
     * @param paramAddress 参数中的地址
     * @param user 用户
     * @return 有效的收货地址
     */
    private String getDeliveryAddress(String paramAddress, User user) {
        if (paramAddress == null || paramAddress.isEmpty()) {
            return Optional.ofNullable(user.getAddress())
                    .orElseThrow(TomatoMallException::noValidAddress);
        }

        return paramAddress;
    }

    /**
     * 获取有效的收货人姓名
     *
     * @param paramName 参数中的姓名
     * @param user 用户
     * @return 有效的收货人姓名
     */
    private String getRecipientName(String paramName, User user) {
        if (paramName == null || paramName.isEmpty()) {
            return user.getUsername();
        }
        return paramName;
    }

    /**
     * 获取有效的收货人电话
     *
     * @param paramPhone 参数中的电话
     * @param user 用户
     * @return 有效的收货人电话
     */
    private String getRecipientPhone(String paramPhone, User user) {
        if (paramPhone == null || paramPhone.isEmpty()) {
            return user.getPhone();
        }
        return paramPhone;
    }

    /**
     * 验证店铺权限
     * 检查当前用户是否有权限操作指定店铺
     *
     * @param storeId 店铺ID
     */
    private void validateStorePermission(int storeId) {
        int userId = securityUtil.getCurrentUser().getId();
        if (!storeRepository.existsByIdAndManagerId(storeId, userId)
                && !employmentRepository.existsByStoreIdAndEmployeeId(storeId, userId)) {
            throw TomatoMallException.permissionDenied();
        }
    }
}