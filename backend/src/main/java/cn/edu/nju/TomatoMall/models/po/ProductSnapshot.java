package cn.edu.nju.TomatoMall.models.po;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "product_snapshot")
public class ProductSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false, updatable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private String description;

    @ElementCollection
    @Column(nullable = false, updatable = false)
    private List<String> images;

    @Column(nullable = false, updatable = false)
    private BigDecimal price;

    @ElementCollection
    @Column(nullable = false, updatable = false)
    private Map<String, String> specifications;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created = LocalDateTime.now();
}
