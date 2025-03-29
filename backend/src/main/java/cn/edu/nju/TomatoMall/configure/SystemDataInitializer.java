package cn.edu.nju.TomatoMall.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.init-data.root-user.id}")
    private Integer userId;

    @Value("${app.init-data.root-user.username}")
    private String username;

    @Value("${app.init-data.root-user.phone}")
    private String phone;

    @Value("${app.init-data.root-user.password}")
    private String password;

    @Value("${app.init-data.system-store.id}")
    private Integer storeId;

    @Value("${app.init-data.system-store.name}")
    private String storeName;

    @Value("${app.init-data.system-store.description}")
    private String storeDescription;

    @Value("${app.init-data.system-store.address}")
    private String storeAddress;

    @Value("${app.init-data.system-store.logo-url}")
    private String storeLogoUrl;

    public SystemDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
        initSystemStore();
    }

    // 使用独立事务确保立即提交
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initAdminUser() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE id = ?",
                Integer.class,
                userId
        );

        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO user (id, username, password, phone, role, create_time) " +
                            "VALUES (?, ?, ?, ?, ?, NOW())",
                    userId, username, password, phone, "ADMIN"
            );
        }
    }

    // 使用独立事务
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initSystemStore() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM store WHERE id = ?",
                Integer.class,
                storeId
        );

        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO store (id, name, description, address, logo_url, create_time, status, manager_id) " +
                            "VALUES (?, ?, ?, ?, ?, NOW(), 'NORMAL', ?)",
                    storeId,
                    storeName,
                    storeDescription,
                    storeAddress,
                    storeLogoUrl,
                    userId
            );

            jdbcTemplate.execute("ALTER TABLE store AUTO_INCREMENT = 1");
        }
    }
}