package cn.edu.nju.TomatoMall.models.po;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    private List<String> images;

    @Column(nullable = false)
    private BigDecimal price;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "product")
    private Inventory inventory;

    private Double rate;

    @NonNull
    private int sales;

    @ElementCollection
    private Map<String, String> specifications;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private boolean onSale;

    @Column(nullable = false)
    private boolean soldOut;

    @PrePersist
    private void prePersist() {
        this.createTime = LocalDateTime.now();
        this.onSale = false;
        this.sales = 0;
    }
}