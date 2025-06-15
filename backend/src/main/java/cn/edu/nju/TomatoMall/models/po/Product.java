package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @ElementCollection
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal price;

    @ElementCollection
    @Builder.Default
    private Map<String, String> specifications  = new HashMap<>();

    private BigDecimal rating;

    @Column(nullable = false)
    @Builder.Default
    private int sales = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Column(nullable = false)
    @Builder.Default
    private boolean onSale = true;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "product")
    @JoinColumn(nullable = false, updatable = false)
    private Inventory inventory;

    @Column(nullable = false)
    @Builder.Default
    private InventoryStatus inventoryStatus = InventoryStatus.OUT_OF_STOCK;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Builder.Default
    private List<ProductSnapshot> snapshots = new ArrayList<>();

    public void createSnapshot() {
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