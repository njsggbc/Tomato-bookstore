package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.store.StoreCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.store.StoreBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreUpdateRequest;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {
    @Autowired
    StoreRepository storeRepository;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    FileUtil fileUtil;

    private static final List<StoreStatus> AWAITING_REVIEW_STATUS = Arrays.asList(StoreStatus.PENDING, StoreStatus.UPDATING);

    @Override
    public Page<StoreBriefResponse> getStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatus(StoreStatus.NORMAL, pageable);

        return storePage.map(StoreBriefResponse::new);
    }

    @Override
    public StoreDetailResponse getDetail(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getStatus().equals(StoreStatus.NORMAL)
                && !securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)
                && !securityUtil.getCurrentUser().equals(store.getManager())
        ) {
            throw TomatoMallException.storeNotFound();
        }
        return new StoreDetailResponse(store);
    }

    @Override
    public StoreBriefResponse getBrief(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        return new StoreBriefResponse(store);
    }

    @Override
    public Boolean createStore(StoreCreateRequest params) {
        String name = params.getName();
        if (storeRepository.findByName(name) != null) {
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

        return true;
    }

    @Override
    public Boolean updateStore(int storeId, StoreUpdateRequest params) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

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

        return true;
    }

    @Override
    public Boolean deleteStore(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

        store.setStatus(StoreStatus.DELETING);

        storeRepository.save(store);

        return true;
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
    public List<String> getStoreTokenList(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }
        return store.getTokens();
    }

    @Override
    public Boolean generateToken(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

        store.getTokens().add(securityUtil.getToken(store.getManager()));
        storeRepository.save(store);
        return true;
    }

    @Override
    public Boolean deleteToken(int storeId, String token) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

        store.getTokens().remove(token);
        storeRepository.save(store);
        return true;
    }

    @Override
    public Boolean authToken(int storeId, String token) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getTokens().contains(token) || !securityUtil.verifyToken(token)) {
            throw TomatoMallException.tokenInvalid();
        }

        User user = securityUtil.getCurrentUser();
        if (store.getManager().equals(user) || store.getStaffs().contains(user)) {
            store.getTokens().remove(token);
            storeRepository.save(store);
            throw TomatoMallException.storeStaffAlreadyExists();
        }

        store.getStaffs().add(user);
        store.getTokens().remove(token);
        storeRepository.save(store);

        return true;
    }

    @Override
    public Boolean deleteStaff(int storeId, int userId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }

        User user = store.getStaffs()
                .stream()
                .filter(staff -> staff.getId() == userId)
                .findFirst()
                .orElseThrow(TomatoMallException::userNotFound);

        store.getStaffs().remove(user);
        storeRepository.save(store);

        return true;
    }

    @Override
    public Boolean review(int storeId, boolean pass) {
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

        return true;
    }

    @Override
    public Page<StoreBriefResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatusIn(AWAITING_REVIEW_STATUS, pageable);

        return storePage.map(StoreBriefResponse::new);
    }

    @Override
    public Page<StoreBriefResponse> getSuspendedStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatus(StoreStatus.SUSPENDED, pageable);

        return storePage.map(StoreBriefResponse::new);
    }

    @Override
    public List<String> getStoreQualification(int storeId) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        return storeRepository.findById(storeId).orElseThrow(TomatoMallException::unexpectedError).getQualifications();
    }

}
