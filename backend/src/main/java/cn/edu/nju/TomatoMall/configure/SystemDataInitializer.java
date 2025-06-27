package cn.edu.nju.TomatoMall.configure;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SystemDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

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

    public SystemDataInitializer(UserRepository userRepository, StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initAdminUser();
        initSystemStore();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initAdminUser() {
        if (!userRepository.existsById(userId)) {
            User adminUser = User.builder()
                    .id(userId)
                    .username(username)
                    .password(password)
                    .phone(phone)
                    .role(Role.ADMIN)
                    .createTime(LocalDateTime.now())
                    .build();

            userRepository.save(adminUser);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initSystemStore() {
        if (!storeRepository.existsById(storeId)) {
            User admin = userRepository.findById(userId)
                    .orElseThrow(TomatoMallException::unexpectedError);

            Store systemStore = Store.builder()
                    .id(storeId)
                    .name(storeName)
                    .description(storeDescription)
                    .address(storeAddress)
                    .logoUrl(storeLogoUrl)
                    .createTime(LocalDateTime.now())
                    .status(StoreStatus.NORMAL)
                    .manager(admin)
                    .isSystemStore(true)
                    .build();

            storeRepository.save(systemStore);
        }
    }
}