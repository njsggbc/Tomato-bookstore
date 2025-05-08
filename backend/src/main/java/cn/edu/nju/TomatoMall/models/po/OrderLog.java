package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.OrderEvent;
import cn.edu.nju.TomatoMall.enums.OrderStatus;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "order_log")
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
