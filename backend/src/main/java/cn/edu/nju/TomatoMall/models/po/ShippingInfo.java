package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.ShippingCompany;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "shipping_infos")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(unique = true)
    private String trackingNumber;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    private ShippingCompany shippingCompany;

    @ElementCollection
    @CollectionTable(name = "shipping_logs", joinColumns = @JoinColumn(name = "shipping_info_id"))
    @MapKeyColumn(name = "log_time")
    @Column(name = "log_message")
    @Builder.Default
    private Map<LocalDateTime, String> logs = new LinkedHashMap<>(); // 时间戳+信息
}
