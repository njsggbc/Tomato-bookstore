package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "payment")
public class Payment {
    @Id
    @Builder.Default
    String id = String.valueOf(System.currentTimeMillis());

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany(mappedBy = "payment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Column(nullable = false)
    List<Order> orders;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime paymentRequestTime;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private LocalDateTime transactionTime;
    private String tradeNo;

    public static PaymentBuilder builder() {
        return new PaymentBuilder(){
            @Override
            public Payment build() {
                Payment payment = super.build();
                if (payment.getOrders() != null) {
                    payment.getOrders().forEach(order -> order.setPayment(payment));
                    payment.setAmount(
                            payment.getOrders().stream()
                                    .map(Order::getTotalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .setScale(2, RoundingMode.HALF_UP)
                    );
                }

                return payment;
            }
        };
    }
}
