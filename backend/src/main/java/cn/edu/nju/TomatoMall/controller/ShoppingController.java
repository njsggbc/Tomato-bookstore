package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.repository.PaymentRepository;
import cn.edu.nju.TomatoMall.service.OrderService;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * shopping
 */
@RestController
@RequestMapping("/api")
public class ShoppingController {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final SecurityUtil securityUtil;

    /**
     * 构造一个新的购物控制器，包含必要的服务和存储库
     *
     * @param orderService 订单操作服务
     * @param paymentService 支付服务
     * @param paymentRepository 支付数据操作存储库
     * @param securityUtil 安全操作工具
     */
    @Autowired
    public ShoppingController(OrderService orderService, PaymentService paymentService, PaymentRepository paymentRepository, SecurityUtil securityUtil) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.securityUtil = securityUtil;
    }


    /*---------------- 通用订单服务 ----------------*/

    /**
     * 获取当前用户的购物车商品列表
     *
     * @return 购物车中的商品项列表
     */
    @GetMapping("/cart")
    public ApiResponse<List<CartItemInfoResponse>> getCartItems() {
        return ApiResponse.success(orderService.getCartItemList());
    }

    /**
     * 向购物车添加商品
     *
     * @param productId 商品ID
     * @param quantity 商品数量
     * @return 操作成功的空响应
     */
    @PostMapping("/cart")
    public ApiResponse<Void> addToCart(@RequestParam int productId,
                                       @RequestParam int quantity) {
        orderService.addToCart(productId, quantity);
        return ApiResponse.success();
    }

    /**
     * 从购物车移除指定项
     *
     * @param cartItemId 要移除的购物车项ID
     * @return 操作成功的空响应
     */
    @DeleteMapping("/cart/{cartItemId}")
    public ApiResponse<Void> removeFromCart(@PathVariable int cartItemId) {
        orderService.removeFromCart(cartItemId);
        return ApiResponse.success();
    }

    /**
     * 修改购物车中商品的数量
     *
     * @param cartItemId 购物车项ID
     * @param quantity 新的商品数量
     * @return 操作成功的空响应
     */
    @PatchMapping("/cart/{cartItemId}")
    public ApiResponse<Void> updateCartQuantity(
            @PathVariable int cartItemId,
            @RequestParam int quantity) {
        orderService.updateCartItemQuantity(cartItemId, quantity);
        return ApiResponse.success();
    }

    /**
     * 结算购物车中的商品
     *
     * @param cartItemIds 购物车项ID列表
     * @return 包含支付信息的响应
     */
    @PostMapping("/cart/checkout")
    public ApiResponse<List<CheckoutResponse>> checkout(@RequestBody List<Integer> cartItemIds) {
        return ApiResponse.success(orderService.checkout(cartItemIds));
    }

    /**
     * 提交订单
     *
     * @param params 提交订单请求参数
     * @return 支付信息响应
     */
    @PostMapping("/orders")
    public ApiResponse<PaymentInfoResponse> submit(@RequestBody SubmitRequest params) {
        return ApiResponse.success(orderService.submit(params));
    }

    /**
     * 获取当前用户的订单列表
     *
     * @param status 可选的订单状态过滤条件
     * @return 订单简要信息列表
     */
    @GetMapping("/orders")
    public ApiResponse<List<OrderBriefResponse>> getOrderList(
            @RequestParam(required = false) CustomerRequestOrderStatus status) {
        return ApiResponse.success(orderService.getOrderList(status));
    }

    /**
     * 获取订单详细信息
     *
     * @param orderId 订单ID
     * @param orderNo 可选的订单编号
     * @return 客户订单详细信息
     */
    @GetMapping("/orders/{orderId}")
    public ApiResponse<CustomerOrderInfoResponse> getOrderDetail(
            @PathVariable Integer orderId,
            @RequestParam(required = false) String orderNo) {
        return ApiResponse.success(orderService.getOrderInfo(orderId, orderNo));
    }

    /**
     * 取消订单
     *
     * @param orderId 要取消的订单ID
     * @param reason 取消原因
     * @return 操作成功的空响应
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @PathVariable int orderId,
            @RequestBody String reason) {
        orderService.cancel(orderId, reason);
        return ApiResponse.success();
    }

    /**
     * 确认收货
     *
     * @param orderId 要确认收货的订单ID
     * @return 操作成功的空响应
     */
    @PostMapping("/orders/{orderId}/confirm")
    public ApiResponse<Void> confirmReceipt(@PathVariable int orderId) {
        orderService.confirmReceipt(orderId);
        return ApiResponse.success();
    }

    /*---------------- 商家订单服务 ----------------*/

    /**
     * 获取店铺的订单列表
     *
     * @param storeId 店铺ID
     * @param status 可选的订单状态过滤条件
     * @return 店铺订单简要信息列表
     */
    @GetMapping("/store/{storeId}/orders")
    public ApiResponse<List<OrderBriefResponse>> getStoreOrders(
            @PathVariable int storeId,
            @RequestParam(required = false) StoreRequestOrderStatus status) {
        return ApiResponse.success(orderService.getStoreOrderList(storeId, status));
    }

    /**
     * 获取店铺订单详细信息
     *
     * @param storeId 店铺ID
     * @param orderId 订单ID
     * @param orderNo 可选的订单编号
     * @return 店铺订单详细信息
     */
    @GetMapping("/store/{storeId}/orders/{orderId}")
    public ApiResponse<StoreOrderInfoResponse> getStoreOrderDetail(
            @PathVariable int storeId,
            @PathVariable int orderId,
            @RequestParam(required = false) String orderNo) {
        return ApiResponse.success(orderService.getStoreOrderInfo(storeId, orderId, orderNo));
    }

    /**
     * 商家确认订单
     *
     * @param storeId 店铺ID
     * @param orderId 要确认的订单ID
     * @return 操作成功的空响应
     */
    @PostMapping("/store/{storeId}/orders/{orderId}/confirm")
    public ApiResponse<Void> confirmOrder(
            @PathVariable int storeId,
            @PathVariable int orderId) {
        orderService.confirm(storeId, orderId);
        return ApiResponse.success();
    }

    /**
     * 商家拒绝订单
     *
     * @param storeId 店铺ID
     * @param orderId 要拒绝的订单ID
     * @param reason 拒绝原因
     * @return 操作成功的空响应
     */
    @PostMapping("/store/{storeId}/orders/{orderId}/refuse")
    public ApiResponse<Void> refuseOrder(
            @PathVariable int storeId,
            @PathVariable int orderId,
            @RequestBody String reason) {
        orderService.refuse(storeId, orderId, reason);
        return ApiResponse.success();
    }

    /**
     * 商家发货
     *
     * @param storeId 店铺ID
     * @param orderId 要发货的订单ID
     * @param params 发货请求参数，包含物流公司和物流单号等信息
     * @return 操作成功的空响应
     */
    @PostMapping("/store/{storeId}/orders/{orderId}/ship")
    public ApiResponse<Void> shipOrder(
            @PathVariable int storeId,
            @PathVariable int orderId,
            @RequestBody ShipRequest params) {
        orderService.ship(storeId, orderId, params);
        return ApiResponse.success();
    }

    /*---------------- 支付服务 ----------------*/

    /**
     * 获取当前用户未支付的支付单列表
     *
     * @return 未支付的支付信息列表
     */
    @GetMapping("/payment/pending")
    public List<PaymentInfoResponse> getPendingPayments() {
        return paymentRepository.findByUserIdAndStatus(securityUtil.getCurrentUser().getId(), PaymentStatus.PENDING)
                .stream()
                .map(PaymentInfoResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 发起支付
     *
     * @param paymentId 支付单ID
     * @param paymentMethod 支付方式
     * @return 支付URL
     * @throws TomatoMallException 当支付参数无效时抛出异常
     */
    @PostMapping("/payment/{paymentId}/pay")
    public ApiResponse<String> pay(
            @PathVariable int paymentId,
            @RequestParam PaymentMethod paymentMethod
    ) {
        String paymentUrl = paymentService.pay(paymentId, paymentMethod);
        return ApiResponse.success(paymentUrl);
    }

    /**
     * 取消支付
     *
     * @param paymentId 要取消的支付单ID
     * @return 操作成功的空响应
     * @throws TomatoMallException 当支付参数无效时抛出异常
     */
    @PostMapping("/payment/{paymentId}/cancel")
    public ApiResponse<Void> cancelPayment(
            @PathVariable int paymentId
    ) {
        paymentService.cancel(paymentId);
        return ApiResponse.success();
    }

    /**
     * 查询交易状态
     *
     * @param paymentNo 支付单号
     * @return 支付宝交易查询响应
     * @throws TomatoMallException 当支付参数无效时抛出异常
     */
    @GetMapping("/payment/trade-status")
    public ApiResponse<Object> getTradeStatus(
            @RequestParam String paymentNo
    ) {
        return ApiResponse.success(paymentService.queryTradeStatus(paymentNo));
    }

    /**
     * 查询退款状态
     *
     * @param paymentNo 支付单号
     * @param orderNo 订单编号
     * @return 支付宝退款查询响应
     * @throws TomatoMallException 当支付参数无效时抛出异常
     */
    @GetMapping("/payment/refund-status")
    public ApiResponse<Object> getRefundStatus(
            @RequestParam String paymentNo,
            @RequestParam String orderNo
    ) {
        return ApiResponse.success(paymentService.queryRefundStatus(paymentNo, orderNo));
    }

    /**
     * 处理支付宝支付通知回调
     *
     * @param request HTTP请求，包含支付宝回调信息
     * @return 处理结果，用于响应支付宝服务器
     */
    @PostMapping("/alipay/notify")
    public String handlePaymentNotify(HttpServletRequest request) {
        return paymentService.handlePaymentNotify(request, PaymentMethod.ALIPAY);
    }
}