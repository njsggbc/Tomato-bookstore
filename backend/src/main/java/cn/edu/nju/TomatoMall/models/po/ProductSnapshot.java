package cn.edu.nju.TomatoMall.models.po;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Immutable
public class ProductSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", updatable = false)
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
    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();
}
