package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.CustomerRequestOrderStatus;
import cn.edu.nju.TomatoMall.enums.StoreRequestOrderStatus;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.order.*;
import cn.edu.nju.TomatoMall.models.dto.shipment.*;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    /*---------------- 通用服务 ----------------*/
    List<CartItemInfoResponse> getCartItemList();

    int addToCart(int productId, int quantity);

    void removeFromCart(int cartItemId);

    void updateCartItemQuantity(int cartItemId, int quantity);

    List<CheckoutResponse> checkout(List<Integer> cartItemIds);

    PaymentInfoResponse submit(SubmitRequest params);

    CustomerOrderInfoResponse getOrderInfo(Integer orderId, String orderNo);

    List<OrderBriefResponse> getOrderList(CustomerRequestOrderStatus status);

    void cancel(int orderId, String message);

    void confirmReceipt(int orderId);

    /*---------------- 商家服务 ----------------*/
    List<OrderBriefResponse> getStoreOrderList(int storeId, StoreRequestOrderStatus status);

    StoreOrderInfoResponse getStoreOrderInfo(int storeId, Integer orderId, String orderNo);

    void confirm(int storeId, int orderId);

    void refuse(int storeId, int orderId, String message);

    void ship(int storeId, int orderId, ShipRequest params);

    /*---------------- 管理员服务 ----------------*/
    void terminate(int orderId);

    /*---------------- 物流服务 ----------------*/
    void updateShippingInfo(String trackingNo, ShippingUpdateRequest params);
    void confirmDelivery(String trackingNo, DeliveryConfirmRequest params);
}
