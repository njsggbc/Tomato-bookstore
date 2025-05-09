package cn.edu.nju.TomatoMall.models.po;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "employment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "store_id"})
})
public class Employment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
}