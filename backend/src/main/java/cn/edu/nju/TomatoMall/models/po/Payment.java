package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    String id = String.valueOf(System.currentTimeMillis());

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany
    @Column(nullable = false)
    List<Order> orders;

    @NonNull
    private BigDecimal amount;

    @NonNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NonNull
    private LocalDateTime createTime;

    private LocalDateTime paymentRequestTime;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private LocalDateTime transactionTime;
    private String tradeNo;

    public Payment(User user, List<Order> orders) {
        this.user = user;
        this.orders = orders;
        this.amount = orders.stream()
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);
        this.status = PaymentStatus.PENDING;
        this.createTime = LocalDateTime.now();
    }
}
