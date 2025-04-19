package cn.edu.nju.TomatoMall.models.po;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    private int quantity;

    @Version
    private Long version;

    // 库存锁定数量 - 用于处理下单流程中的库存锁定
    private Integer lockedQuantity = 0;

    // 预警阈值
    private Integer thresholdQuantity;
}
