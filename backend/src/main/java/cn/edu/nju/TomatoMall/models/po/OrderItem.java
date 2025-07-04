package cn.edu.nju.TomatoMall.models.po;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;

// 订单项，订单为空时表示在购物车中

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private ProductSnapshot productSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    public static OrderItemBuilder builder() {
        return new OrderItemBuilder() {
            @Override
            public OrderItem build() {
                OrderItem item = super.build();
                if (item.getProductSnapshot() != null && item.getQuantity() > 0) {
                    item.setTotalPrice(
                            item.getProductSnapshot().getPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    );
                }
                return item;
            }
        };
    }
}
