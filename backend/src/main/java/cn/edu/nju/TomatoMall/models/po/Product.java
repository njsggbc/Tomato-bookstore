package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal price;

    @ElementCollection
    private Map<String, String> specifications  = new HashMap<>();

    private Double rate;

    @Column(nullable = false)
    private int sales = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Column(nullable = false)
    private boolean onSale = true;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "product")
    @JoinColumn(nullable = false, updatable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private InventoryStatus inventoryStatus = InventoryStatus.OUT_OF_STOCK;

    @OneToMany
    private List<ProductSnapshot> snapshots = new ArrayList<>();

    public void update() {
        snapshots.add(ProductSnapshot.builder()
                        .product(this)
                        .name(name)
                        .description(description)
                        .images(images)
                        .price(price)
                        .specifications(specifications)
                        .build());
    }

    public ProductSnapshot getSnapshot() {
        return snapshots.get(snapshots.size() - 1);
    }
}