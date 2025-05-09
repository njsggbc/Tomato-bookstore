package cn.edu.nju.TomatoMall.models.po;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    @Builder.Default
    private int quantity = 0;

    @Version
    private Long version;

    // 库存锁定数量 - 用于处理下单流程中的库存锁定
    @Builder.Default
    private Integer lockedQuantity = 0;

    // 预警阈值
    @Builder.Default
    private Integer thresholdQuantity = 5;
}
