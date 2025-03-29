package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.StoreStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.session.StoreType;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    private Integer score;
    private Integer scoreCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "store_staff",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> staffs;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Product> products;

    @ElementCollection
    private List<String> tokens;

    @ElementCollection
    private List<String> qualifications;
}