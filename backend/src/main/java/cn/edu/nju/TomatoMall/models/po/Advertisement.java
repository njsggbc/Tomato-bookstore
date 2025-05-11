package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.AdStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advertisements")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content; // 广告图片url

    @Column(nullable = false)
    private String linkUrl; // 跳转链接

    @Column(nullable = false)
    @Builder.Default
    private AdStatus status = AdStatus.DISABLED;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private Store store;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
