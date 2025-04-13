package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.StoreRole;
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.PermissionService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final EmploymentRepository employmentRepository;
    private final StoreRepository storeRepository;
    private final SecurityUtil securityUtil;

    public PermissionServiceImpl(EmploymentRepository employmentRepository,
                                 StoreRepository storeRepository,
                                 SecurityUtil securityUtil) {
        this.employmentRepository = employmentRepository;
        this.storeRepository = storeRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    public StoreRole getStoreRole(int storeId) {
        int userId = securityUtil.getCurrentUser().getId();
        if (storeRepository.existsByIdAndManagerId(storeId, userId)) {
            return StoreRole.MANAGER;
        }
        if (employmentRepository.existsByStoreIdAndEmployeeId(storeId, userId)) {
            return StoreRole.STAFF;
        } else {
            return StoreRole.CUSTOMER;
        }
    }
}
