package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.OrderEvent;
import cn.edu.nju.TomatoMall.enums.OrderStatus;
import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Immutable
public class OrderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderEvent event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus afterEventStatus;

    private String message;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
