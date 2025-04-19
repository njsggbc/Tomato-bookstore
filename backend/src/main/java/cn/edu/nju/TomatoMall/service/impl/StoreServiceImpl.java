package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.store.StoreCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreUpdateRequest;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.StoreService;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final SecurityUtil securityUtil;
    private final FileUtil fileUtil;
    private static final List<StoreStatus> AWAITING_REVIEW_STATUS = Arrays.asList(StoreStatus.PENDING, StoreStatus.UPDATING);
    private final EmploymentRepository employmentRepository;

    @Autowired
    public StoreServiceImpl(StoreRepository storeRepository, SecurityUtil securityUtil, FileUtil fileUtil, EmploymentRepository employmentRepository) {
        this.storeRepository = storeRepository;
        this.securityUtil = securityUtil;
        this.fileUtil = fileUtil;
        this.employmentRepository = employmentRepository;
    }

    @Override
    public Page<StoreInfoResponse> getStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatus(StoreStatus.NORMAL, pageable);

        return storePage.map(StoreInfoResponse::new);
    }

    @Override
    public List<StoreInfoResponse> getManagedStoreList() {
        return storeRepository.findByManagerId(securityUtil.getCurrentUser().getId()).stream()
                .map(StoreInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreInfoResponse> getWorkedStoreList() {
        return employmentRepository.getStoreByEmployeeId(securityUtil.getCurrentUser().getId()).stream()
                .map(StoreInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public StoreInfoResponse getInfo(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getStatus().equals(StoreStatus.NORMAL)
                && !securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)
                && !securityUtil.getCurrentUser().equals(store.getManager()) // 普通用户只能查看正常店铺的信息
        ) {
            throw TomatoMallException.storeNotFound();
        }
        return new StoreInfoResponse(store);
    }

    @Override
    public void createStore(StoreCreateRequest params) {
        String name = params.getName();
        if (storeRepository.existsByName(name)) {
            throw TomatoMallException.storeNameAlreadyExists();
        }

        User user = securityUtil.getCurrentUser();
        Store store = new Store();

        store.setName(params.getName());
        store.setManager(user);
        store.setCreateTime(LocalDateTime.now());
        store.setStatus(StoreStatus.PENDING);
        store.setLogoUrl(fileUtil.upload(user.getId(), params.getLogo()));
        store.setAddress(params.getAddress());
        store.setDescription(params.getDescription());
        store.setQualifications(params.getQualification().stream()
                .map(qualification -> fileUtil.upload(user.getId(), qualification))
                .collect(Collectors.toList())
        );

        storeRepository.save(store);
    }

    @Override
    public void updateStore(int storeId, StoreUpdateRequest params) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        validatePermission(store);

        if (params.getName() != null) {
            store.setName(params.getName());
        }
        if (params.getLogo() != null) {
            if(store.getLogoUrl() != null){
                fileUtil.delete(store.getLogoUrl());
            }
            store.setLogoUrl(fileUtil.upload(store.getManager().getId(), params.getLogo()));
        }
        if (params.getAddress() != null) {
            store.setAddress(params.getAddress());
        }
        if (params.getDescription() != null) {
            store.setDescription(params.getDescription());
        }
        if (params.getQualification() != null) {
            store.getQualifications().forEach(fileUtil::delete);
            store.setQualifications(params.getQualification().stream()
                    .map(qualification -> fileUtil.upload(store.getManager().getId(), qualification))
                    .collect(Collectors.toList())
            );
        }

        store.setStatus(StoreStatus.UPDATING);

        storeRepository.save(store);
    }

    @Override
    public void deleteStore(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        validatePermission(store);

        store.setStatus(StoreStatus.DELETING);

        storeRepository.save(store);
    }

    private void deleteStore(Store store){
        if(store.getLogoUrl() != null){
            fileUtil.delete(store.getLogoUrl());
        }
        if(store.getQualifications() != null){
            store.getQualifications().forEach(fileUtil::delete);
        }

        storeRepository.delete(store);
    }

    @Override
    public void review(int storeId, boolean pass) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);

        switch (store.getStatus()) {
            case PENDING:
                if (pass) {
                    store.setStatus(StoreStatus.NORMAL);
                    storeRepository.save(store);
                } else {
                    deleteStore(store);
                }
                break;
            case DELETING:
                if (pass) {
                    storeRepository.delete(store);
                } else {
                    store.setStatus(StoreStatus.SUSPENDED);
                    storeRepository.save(store);
                }
                break;
            case UPDATING:
                if (pass) {
                    store.setStatus(StoreStatus.NORMAL);
                    storeRepository.save(store);
                } else {
                    store.setStatus(StoreStatus.SUSPENDED);
                    storeRepository.save(store);
                }
                break;
            default: throw TomatoMallException.invalidOperation();
        }
    }

    @Override
    public Page<StoreInfoResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatusIn(AWAITING_REVIEW_STATUS, pageable);

        return storePage.map(StoreInfoResponse::new);
    }

    @Override
    public Page<StoreInfoResponse> getSuspendedStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatus(StoreStatus.SUSPENDED, pageable);

        return storePage.map(StoreInfoResponse::new);
    }

    @Override
    public List<String> getStoreQualification(int storeId) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        return storeRepository.findById(storeId).orElseThrow(TomatoMallException::unexpectedError).getQualifications();
    }

    private void validatePermission(Store store) {
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }
    }
}
