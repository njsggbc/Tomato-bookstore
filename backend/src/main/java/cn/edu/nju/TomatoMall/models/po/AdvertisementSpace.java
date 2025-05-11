package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "advertisement_spaces")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AdvertisementSpace implements Serializable {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String label;

    @Column(nullable = false)
    private AdSpaceType type;

    @ManyToOne(fetch = FetchType.EAGER)
    private AdvertisementSlot current; // 当前展示槽位

    @ManyToOne(fetch = FetchType.EAGER)
    private AdvertisementSlot forbiddenEdge; // 24小时内不可投放区域的边界，指向时间范围内第一个可用槽位

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "space")
    private List<AdvertisementSlot> slots;

    @Column(nullable = false)
    @Builder.Default
    private int cycle = 7; // 周期, 单位天, 默认7天

    @Column(nullable = false)
    @Builder.Default
    private int segment = 3; // 广告展示时间段, 单位小时, 默认3小时


    @Transient
    @Transactional
    public Advertisement tick() {
        Advertisement expired = current.getAdvertisement();

        current.setAdvertisement(null);
        current.increaseStartTime(cycle * 24); // 将时间推移至下一个周期
        current.setAvailable(true);
        current.setActive(false);
        current = current.getNext();

        forbiddenEdge.setAvailable(false);
        forbiddenEdge = forbiddenEdge.getNext();

        return expired;
    }
}