package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.AdPlacementStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "advertisement_placements")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AdvertisementPlacement {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Advertisement advertisement; // 广告

    @ManyToOne
    private AdvertisementSpace space; // 广告位

    @Column(nullable = false)
    @Builder.Default
    private AdPlacementStatus status = AdPlacementStatus.PENDING;

    private String comment; // 备注信息

    @ElementCollection
    private List<LocalDateTime> displayTimeList; // 展示时间列表

    @ManyToMany
    private List<AdvertisementSlot> slotList; // 槽位列表

    @Column(nullable = false)
    private int displayDuration; // 每次展示时长，单位小时

    @OneToOne
    private Payment payment;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间
}
