package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.CustomerRequestOrderStatus;
import cn.edu.nju.TomatoMall.enums.StoreRequestOrderStatus;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.dto.shipment.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理服务接口
 */
public interface OrderService {
    /**
     * 获取当前用户的购物车商品列表
     * @return 购物车项信息列表
     */
    List<CartItemInfoResponse> getCartItemList();

    /**
     * 向购物车添加商品
     * @param productId 商品ID
     * @param quantity 数量
     * @return 购物车项ID
     */
    int addToCart(int productId, int quantity);

    /**
     * 从购物车移除指定项
     * @param cartItemId 购物车项ID
     */
    void removeFromCart(int cartItemId);

    /**
     * 修改购物车中商品的数量
     * @param cartItemId 购物车项ID
     * @param quantity 数量
     */
    void updateCartItemQuantity(int cartItemId, int quantity);

    /**
     * 结算购物车中的商品
     * @param cartItemIds 购物车项ID列表
     * @return 结算信息列表
     */
    List<CheckoutResponse> checkout(List<Integer> cartItemIds);

    /**
     * 提交订单
     * @param cartItemIds 购物车项ID列表
     * @param recipientName 收件人姓名
     * @param recipientPhone 收件人电话
     * @param recipientAddress 收件人地址
     * @param storeRemarks 商店备注
     * @return 支付信息
     */
    PaymentInfoResponse submit(
            List<Integer> cartItemIds,
            String recipientName,
            String recipientPhone,
            String recipientAddress,
            Map<Integer, String> storeRemarks
    );

    /**
     * 获取订单详细信息
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 订单详情
     */
    CustomerOrderInfoResponse getOrderInfo(Integer orderId, String orderNo);

    /**
     * 获取当前用户的订单列表
     * @param status 订单状态
     * @return 订单简要信息列表
     */
    List<OrderBriefResponse> getOrderList(CustomerRequestOrderStatus status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param message 取消原因
     */
    void cancel(int orderId, String message);

    /**
     * 确认收货
     * @param orderId 订单ID
     */
    void confirmReceipt(int orderId);

    /*---------------- 商家服务 ----------------*/

    /**
     * 获取店铺的订单列表
     * @param storeId 商店ID
     * @param status 订单状态
     * @return 订单简要信息列表
     */
    List<OrderBriefResponse> getStoreOrderList(int storeId, StoreRequestOrderStatus status);

    /**
     * 获取店铺订单详细信息
     * @param storeId 商店ID
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @return 商家视角订单详情
     */
    StoreOrderInfoResponse getStoreOrderInfo(int storeId, Integer orderId, String orderNo);

    /**
     * 商家确认订单
     * @param storeId 商店ID
     * @param orderId 订单ID
     */
    void confirm(int storeId, int orderId);

    /**
     * 商家拒绝订单
     * @param storeId 商店ID
     * @param orderId 订单ID
     * @param message 拒绝原因
     */
    void refuse(int storeId, int orderId, String message);

    /**
     * 商家发货
     * @param storeId 商店ID
     * @param orderId 订单ID
     * @param params 发货请求参数
     */
    void ship(int storeId, int orderId, ShipRequest params);

    /*---------------- 管理员服务 ----------------*/

    /**
     * 终止订单（管理员权限）
     * @param orderId 订单ID
     */
    void terminate(int orderId);

    /*---------------- 物流服务 ----------------*/

    /**
     * 更新物流信息
     * @param trackingNo 物流单号
     * @param params 更新请求参数
     */
    void updateShippingInfo(String trackingNo, ShippingUpdateRequest params);

    /**
     * 物流公司确认送达
     * @param trackingNo 物流单号
     * @param params 确认送达请求参数
     */
    void confirmDelivery(String trackingNo, DeliveryConfirmRequest params);
}
