package cn.edu.nju.TomatoMall.models.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

// 订单项，订单为空时表示在购物车中

@Entity
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    public Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    public Product product;

    @Column(nullable = false)
    public int quantity;

    @Column(updatable = false)
    public BigDecimal unitPriceSnapshot; // 商品单价快照，下单时设置
}
