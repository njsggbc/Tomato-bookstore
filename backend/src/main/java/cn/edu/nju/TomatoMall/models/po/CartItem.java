package cn.edu.nju.TomatoMall.models.po;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

}
