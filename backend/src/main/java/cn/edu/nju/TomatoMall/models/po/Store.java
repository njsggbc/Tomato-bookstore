package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.StoreStatus;
import lombok.*;
import org.springframework.boot.autoconfigure.session.StoreType;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    private Integer score;
    private Integer scoreCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @ElementCollection
    private List<String> qualifications;
}