package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private String name;
    private String email;
    private String avatarUrl;
    private String address;

    // 关联关系
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Store> managedStores = new ArrayList<>();

    @ManyToMany(mappedBy = "staffs", fetch = FetchType.LAZY)
    private List<Store> workedStores = new ArrayList<>();

}
