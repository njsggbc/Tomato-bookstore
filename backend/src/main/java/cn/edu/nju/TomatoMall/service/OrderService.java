package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.CustomerRequestOrderStatus;
import cn.edu.nju.TomatoMall.enums.OrderEvent;
import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.enums.StoreRequestOrderStatus;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.dto.shipment.*;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.User;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 订单管理服务接口
 */
public interface OrderService {

    /**
     * 获取购物车项列表
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 购物车项信息分页
     */
    Page<CartItemInfoResponse> getCartItemList(int page, int size, String field, boolean order);

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
    Page<OrderBriefResponse> getOrderList(int page, int size, String field, boolean order, CustomerRequestOrderStatus status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param message 取消原因
     */
    void cancel(int orderId, String message);

    /**
     * 内部取消订单方法，用于内部逻辑处理
     * @param orderId 订单ID
     * @param reason 取消原因
     */
    void cancelInternal(int orderId, String reason);

    /**
     * 确认收货
     * @param orderId 订单ID
     */
    void confirmReceipt(int orderId);

    /*---------------- 商家服务 ----------------*/

    /**
     * 获取商店的订单列表
     * @param storeId 商店ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @param status 订单状态
     * @return 商家视角订单简要信息分页
     */
    Page<OrderBriefResponse> getStoreOrderList(int storeId,
                                               int page,
                                               int size,
                                               String field,
                                               boolean order,
                                               StoreRequestOrderStatus status);

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

    /* ---------------- 辅助方法 ----------------*/

    /**
     * 更新订单状态
     * @param order 订单对象
     * @param operator 操作人员
     * @param event 订单事件
     * @param status 新的订单状态
     * @param message 附加消息
     */
    void updateStatus(Order order, User operator, OrderEvent event, OrderStatus status, String message);
}
