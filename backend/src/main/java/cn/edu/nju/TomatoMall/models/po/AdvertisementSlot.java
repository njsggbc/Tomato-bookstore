package cn.edu.nju.TomatoMall.models.po;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advertisement_slots")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AdvertisementSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "space_id",nullable = false, updatable = false)
    private AdvertisementSpace space;

    @OneToOne
    private AdvertisementSlot prev;

    @OneToOne
    private AdvertisementSlot next;

    @ManyToOne
    private Advertisement advertisement;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true; // 是否允许投放

    @Column(nullable = false)
    @Builder.Default
    private boolean active = false; // 广告是否可以展示

    @Column(nullable = false)
    private LocalDateTime startTime;

    public void increaseStartTime(int hours) {
        if (startTime != null) {
            startTime = startTime.plusHours(hours);
        }
    }

}
