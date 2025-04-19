package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.events.order.OrderRefundEvent;
import cn.edu.nju.TomatoMall.events.order.OrderRefundSuccessEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCancelEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCreateEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentSuccessEvent;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.po.*;
import cn.edu.nju.TomatoMall.repository.*;
import cn.edu.nju.TomatoMall.service.InventoryService;
import cn.edu.nju.TomatoMall.service.OrderService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SecurityUtil securityUtil;
    private final StoreRepository storeRepository;
    private final EmploymentRepository employmentRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderServiceImpl(ProductRepository productRepository,
                            OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            SecurityUtil securityUtil,
                            StoreRepository storeRepository,
                            EmploymentRepository employmentRepository,
                            InventoryService inventoryService,
                            ApplicationEventPublisher eventPublisher
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.securityUtil = securityUtil;
        this.storeRepository = storeRepository;
        this.employmentRepository = employmentRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    //---------------------------
    // 客户相关方法
    //---------------------------

    /**
     * 购买单个商品并创建订单
     *
     * @param params 购买请求参数
     * @return 支付信息响应
     */
    @Override
    @Transactional
    public PaymentInfoResponse purchase(PurchaseRequest params) {
        // 创建包含单个商品的订单
        Order order = buildOrder(
                securityUtil.getCurrentUser(),
                Collections.singletonList(buildOrderItem(securityUtil.getCurrentUser(), params.getProductId(), params.getQuantity())),
                params.getRemark(),
                params.getAddress(),
                params.getName(),
                params.getPhone()
        );

        // 为订单创建支付
        Payment payment = new Payment(securityUtil.getCurrentUser(), Collections.singletonList(order));
        order.setPayment(payment);

        // 发布支付创建事件
        eventPublisher.publishEvent(new PaymentCreateEvent(payment));

        orderRepository.save(order);
        return new PaymentInfoResponse(payment);
    }

    /**
     * 处理多个购物车项的结算
     * 按店铺分组商品并为每个店铺创建独立的订单
     *
     * @param params 结算请求参数
     * @return 支付信息响应
     */
    @Override
    @Transactional
    public PaymentInfoResponse checkout(CheckOutRequest params) {
        User user = securityUtil.getCurrentUser();

        // 获取并验证购物车项
        List<OrderItem> cartItems = getValidCartItems(user, params.getCartItemIds());

        // 按店铺分组商品并创建订单
        List<Order> orders = groupByStore(cartItems).entrySet().stream()
                .map(entry -> buildOrder(
                        user,
                        entry.getKey(),
                        entry.getValue(),
                        params.getAddress(),
                        params.getName(),
                        params.getPhone(),
                        params.getRemark()
                ))
                .collect(Collectors.toList());

        // 为所有订单创建单一支付
        Payment payment = new Payment(securityUtil.getCurrentUser(), orders);
        orders.forEach(order -> order.setPayment(payment));

        orderRepository.saveAll(orders);

        // 发布支付创建事件
        eventPublisher.publishEvent(new PaymentCreateEvent(payment));

        return new PaymentInfoResponse(payment);
    }

    /**
     * 获取当前用户的购物车项列表
     *
     * @return 购物车项信息列表
     */
    @Override
    public List<OrderItemInfoResponse> getCartItems() {
        User user = securityUtil.getCurrentUser();
        return orderItemRepository.findCartItemsByUserId(user.getId()).stream()
                .map(OrderItemInfoResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 添加商品到购物车
     *
     * @param params 添加购物车请求参数
     */
    @Override
    public void addToCart(CartAddRequest params) {
        try {
            orderItemRepository.save(buildOrderItem(securityUtil.getCurrentUser(), params.getProductId(), params.getQuantity()));
        } catch (Exception e) {
            throw TomatoMallException.invalidOperation();
        }
    }

    /**
     * 从购物车中移除商品
     *
     * @param cartItemId 购物车项ID
     */
    @Override
    public void removeFromCart(int cartItemId) {
        try {
            orderItemRepository.deleteCartItemByIdAndUserId(cartItemId, securityUtil.getCurrentUser().getId());
        } catch (Exception e) {
            throw TomatoMallException.invalidOperation();
        }
    }

    /**
     * 更新购物车中商品的数量
     *
     * @param cartItemId 购物车项ID
     * @param quantity 更新的数量
     */
    @Override
    public void updateCartItemQuantity(int cartItemId, int quantity) {
        try {
            orderItemRepository.updateCartItemQuantityByIdAndUserId(cartItemId, securityUtil.getCurrentUser().getId(), quantity);
        } catch (Exception e) {
            throw TomatoMallException.invalidOperation();
        }
    }

    /**
     * 获取订单详细信息
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 客户订单详细信息
     */
    @Override
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
     * @return 订单简要信息列表
     */
    @Override
    public List<OrderBriefResponse> getOrderList(CustomerRequestOrderStatus status) {
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
                statusList = OrderStatus.terminalStatus();
                break;
            case AFTER_SALE:
                statusList = OrderStatus.afterSaleStatus();
                break;
            default:
                throw TomatoMallException.invalidOperation();
        }

        return orderRepository.findByUserIdAndStatusIn(securityUtil.getCurrentUser().getId(), statusList).stream()
                .map(OrderBriefResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     */
    @Override
    public void cancel(int orderId, String reason) {
        Order order = orderRepository.findByIdAndUserId(orderId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.PROCESSING
                && order.getStatus() != OrderStatus.AWAITING_SHIPMENT) {
            throw TomatoMallException.invalidOperation();
        }

        order.getItems().forEach(this::cancelOrderItem);
        order.setStatus(OrderStatus.REFUND_PROCESSING);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.CANCEL,
                OrderStatus.REFUND_PROCESSING,
                "用户取消订单: " + reason,
                LocalDateTime.now()
        ));

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderRefundEvent(order, order.getTotalAmount(),"用户取消订单"));
    }

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     */
    @Override
    public void confirmReceipt(int orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.AWAITING_RECEIPT) {
            throw TomatoMallException.invalidOperation();
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.CONFIRM_RECEIPT,
                OrderStatus.COMPLETED,
                "确认收货",
                LocalDateTime.now()
        ));

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
     * @return 订单简要信息列表
     */
    @Override
    public List<OrderBriefResponse> getStoreOrderList(int storeId, StoreRequestOrderStatus status) {
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
                statusList = OrderStatus.terminalStatus();
                break;
            case AFTER_SALE:
                statusList = OrderStatus.afterSaleStatus();
                break;
            default:
                throw TomatoMallException.invalidOperation();
        }

        return orderRepository.findByStoreIdAndStatusIn(storeId, statusList).stream()
                .map(OrderBriefResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取店铺订单详细信息
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 店铺订单详细信息
     */
    @Override
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
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     */
    @Override
    public void confirm(int storeId, int orderId) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw TomatoMallException.invalidOperation();
        }

        order.getItems().forEach(item -> {
            inventoryService.confirmStockDeduction(item.getProduct().getId(), item.getQuantity());
        });
        order.setStatus(OrderStatus.AWAITING_SHIPMENT);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.CONFIRM,
                OrderStatus.AWAITING_SHIPMENT,
                "订单已确认",
                LocalDateTime.now()
        ));

        orderRepository.save(order);
    }

    /**
     * 商家拒绝订单
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param message 拒绝原因
     */
    @Override
    public void refuse(int storeId, int orderId, String message) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw TomatoMallException.invalidOperation();
        }

        order.getItems().forEach(this::cancelOrderItem);
        order.setStatus(OrderStatus.REFUND_PROCESSING);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.REFUSE,
                OrderStatus.REFUND_PROCESSING,
                "商家取消订单: " + message,
                LocalDateTime.now()
        ));

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderRefundEvent(order, order.getTotalAmount(),"商家取消订单"));
    }

    /**
     * 商家发货
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param params 发货请求参数
     */
    @Override
    public void ship(int storeId, int orderId, ShipRequest params) {
        validateStorePermission(storeId);

        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(TomatoMallException::orderNotFound);
        if (order.getStatus() != OrderStatus.AWAITING_SHIPMENT) {
            throw TomatoMallException.invalidOperation();
        }

        // TODO: 物流发货逻辑，验证并关联订单

        // 更新销量
        order.getItems().forEach(item -> {
            productRepository.increaseSalesById(item.getProduct().getId(), item.getQuantity());
        });

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.SHIP,
                OrderStatus.IN_TRANSIT,
                "已发货",
                LocalDateTime.now()
        ));

        orderRepository.save(order);
    }

    //-----------------------------
    // 管理员相关方法
    //-----------------------------

    /**
     * 终止订单（管理员功能）
     *
     * @param orderId 订单ID
     */
    @Override
    public void terminate(int orderId) {
        // TODO
    }

    //-----------------------------
    // 事件监听处理
    //-----------------------------

    /**
     * 处理支付成功事件
     *
     * @param event 支付成功事件
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        Payment payment = event.getPayment();
        payment.getOrders().forEach(order -> {
            order.setStatus(OrderStatus.PROCESSING);
            order.getLogs().add(buildOrderLog(
                    order.getUser(),
                    order,
                    OrderEvent.PAY,
                    OrderStatus.PROCESSING,
                    "交易号：" + payment.getTradeNo(),
                    LocalDateTime.now()
            ));
        });

        orderRepository.saveAll(payment.getOrders());
    }

    /**
     * 处理支付取消事件
     *
     * @param event 支付取消事件
     */
    @EventListener
    @Transactional
    public void handlePaymentCancel(PaymentCancelEvent event) {
        Payment payment = event.getPayment();
        payment.getOrders().forEach(order -> {
            order.getItems().forEach(this::cancelOrderItem);
            order.setStatus(OrderStatus.CANCELLED);
            order.setPayment(null);
            order.getLogs().add(buildOrderLog(
                    securityUtil.getCurrentUser(),
                    order,
                    OrderEvent.CANCEL,
                    OrderStatus.CANCELLED,
                    "支付取消: " + event.getReason(),
                    LocalDateTime.now()
            ));
        });

        orderRepository.saveAll(payment.getOrders());
    }

    /**
     * 处理订单退款成功事件
     *
     * @param event 订单退款成功事件
     */
    @EventListener
    @Transactional
    public void handleOrderRefundSuccess(OrderRefundSuccessEvent event) {
        Order order = event.getOrder();
        order.setStatus(OrderStatus.CANCELLED);
        order.getLogs().add(buildOrderLog(
                securityUtil.getCurrentUser(),
                order,
                OrderEvent.REFUND,
                OrderStatus.CANCELLED,
                "已退款: " + event.getRefundAmount() + "\n" +
                        "交易号：" + event.getTradeNo(),
                LocalDateTime.now()
        ));

        orderRepository.save(order);
    }

    //-----------------------------
    // 辅助方法
    //-----------------------------

    /**
     * 构建订单（简化版本，使用已有商品列表）
     *
     * @param user 用户
     * @param items 订单项列表
     * @param remark 订单备注
     * @param address 收货地址
     * @param name 收货人姓名
     * @param phone 收货人电话
     * @return 构建的订单对象
     */
    private Order buildOrder(User user,
                             List<OrderItem> items,
                             String remark,
                             String address,
                             String name,
                             String phone
    ) {
        if (items.isEmpty()) {
            throw TomatoMallException.invalidOrderItem();
        }
        return buildOrder(user, items.get(0).getProduct().getStore(), items, address, name, phone, remark);
    }

    /**
     * 构建订单（完整版本，指定店铺和商品列表）
     *
     * @param user 用户
     * @param store 店铺
     * @param items 订单项列表
     * @param address 收货地址
     * @param name 收货人姓名
     * @param phone 收货人电话
     * @param remark 订单备注
     * @return 构建的订单对象
     */
    private Order buildOrder(User user,
                             Store store,
                             List<OrderItem> items,
                             String address,
                             String name,
                             String phone,
                             String remark
    ) {
        if (items.isEmpty()) {
            throw TomatoMallException.invalidOrderItem();
        }
        Order order = new Order();
        order.setUser(user);
        order.setStore(store);
        order.setRemark(remark);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);

        // 处理订单项，并计算总价
        BigDecimal total = items.stream()
                .map(item -> processOrderItem(order, item))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // 添加订单项
        items.forEach(order.getItems()::add);

        // 添加收货信息
        order.getShippingInfos().add(buildShippingInfo(
                user,
                address,
                name,
                phone
        ));

        order.setCreateTime(LocalDateTime.now());

        // 添加订单日志
        order.getLogs().add(buildOrderLog(
                user,
                order,
                OrderEvent.CREATE,
                OrderStatus.AWAITING_PAYMENT,
                "下单成功",
                order.getCreateTime()
        ));

        return order;
    }

    /**
     * 构建订单项
     *
     * @param user 用户
     * @param productId 商品ID
     * @param quantity 数量
     * @return 构建的订单项对象
     */
    private OrderItem buildOrderItem(User user, int productId, int quantity) {
        if (!productRepository.existsByIdAndOnSaleIsTrue(productId)) {
            throw TomatoMallException.productNotFound();
        }
        OrderItem item = new OrderItem();
        item.setProduct(productRepository.getReferenceById(productId));
        item.setQuantity(quantity);
        item.setUser(user);
        return item;
    }

    /**
     * 处理订单项，锁定库存并计算价格
     *
     * @param order 订单
     * @param orderItem 订单项
     * @return 订单项总价
     */
    private BigDecimal processOrderItem(Order order, OrderItem orderItem) {
        orderItem.setOrder(order);
        Product product = orderItem.getProduct();
        inventoryService.lockStock(product.getId(), orderItem.getQuantity());
        orderItem.setUnitPriceSnapshot(product.getPrice());
        return product.getPrice().multiply(new BigDecimal(orderItem.getQuantity()));
    }

    /**
     * 取消订单项，解锁库存并返回到购物车
     *
     * @param orderItem 订单项
     */
    private void cancelOrderItem(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        inventoryService.unlockStock(product.getId(), orderItem.getQuantity());

        // 查找用户是否已有该商品的购物车项
        Optional<OrderItem> existingCartItem = orderItemRepository.findCartItemByUserIdAndProductId(
                orderItem.getUser().getId(),
                product.getId()
        );

        if (existingCartItem.isPresent()) {
            // 合并数量
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + orderItem.getQuantity());
            orderItemRepository.save(existingCartItem.get());
        } else {
            // 新建购物车项
            orderItemRepository.save(buildOrderItem(orderItem.getUser(), product.getId(), orderItem.getQuantity()));
        }
    }

    /**
     * 构建收货信息
     *
     * @param user 用户
     * @param address 收货地址
     * @param name 收货人姓名
     * @param phone 收货人电话
     * @return 构建的收货信息对象
     */
    private ShippingInfo buildShippingInfo(User user, String address, String name, String phone) {
        ShippingInfo info = new ShippingInfo();
        info.setDeliveryAddress(getDeliveryAddress(address, user));
        info.setRecipientName(getRecipientName(name, user));
        info.setRecipientPhone(getRecipientPhone(phone, user));
        return info;
    }

    /**
     * 构建订单日志
     *
     * @param user 操作用户
     * @param order 订单
     * @param event 订单事件
     * @param status 事件后的状态
     * @param message 日志信息
     * @param timestamp 时间戳
     * @return 构建的订单日志对象
     */
    private OrderLog buildOrderLog(User user, Order order, OrderEvent event, OrderStatus status, String message, LocalDateTime timestamp) {
        OrderLog log = new OrderLog();
        log.setOrder(order);
        log.setEvent(event);
        log.setAfterEventStatus(status);
        log.setMessage(message);
        log.setOperator(user);
        log.setTimestamp(timestamp);
        return log;
    }

    /**
     * 获取有效的购物车项
     *
     * @param user 用户
     * @param cartItemIds 购物车项ID列表
     * @return 有效的购物车项列表
     */
    private List<OrderItem> getValidCartItems(User user, List<Integer> cartItemIds) {
        List<OrderItem> items = orderItemRepository.findCartItemsByIdsAndUserId(cartItemIds, user.getId());
        if (items.size() != cartItemIds.size()) {
            throw TomatoMallException.invalidOrderItem();
        }
        return items;
    }

    /**
     * 将订单项按店铺分组
     *
     * @param items 订单项列表
     * @return 按店铺分组的订单项映射
     */
    private Map<Store, List<OrderItem>> groupByStore(List<OrderItem> items) {
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
        return Optional.ofNullable(paramAddress)
                .orElse(Optional.ofNullable(user.getAddress())
                        .orElseThrow(TomatoMallException::noValidAddress));
    }

    /**
     * 获取有效的收货人姓名
     *
     * @param paramName 参数中的姓名
     * @param user 用户
     * @return 有效的收货人姓名
     */
    private String getRecipientName(String paramName, User user) {
        return Optional.ofNullable(paramName)
                .orElse(Optional.ofNullable(user.getName())
                        .orElseGet(user::getUsername));
    }

    /**
     * 获取有效的收货人电话
     *
     * @param paramPhone 参数中的电话
     * @param user 用户
     * @return 有效的收货人电话
     */
    private String getRecipientPhone(String paramPhone, User user) {
        return Optional.ofNullable(paramPhone)
                .orElseGet(user::getPhone);
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