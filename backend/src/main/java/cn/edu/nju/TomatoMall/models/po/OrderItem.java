package cn.edu.nju.TomatoMall.models.po;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

// 订单项，订单为空时表示在购物车中

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "order_item")
@Immutable
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private int productId;

    @ManyToOne
    @JoinColumn(name = "product_snapshot_id", nullable = false)
    private ProductSnapshot product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @PrePersist
    private void calculateTotalPrice() {
        this.totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
