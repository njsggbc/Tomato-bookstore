package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.StoreService;
import cn.edu.nju.TomatoMall.service.impl.events.store.StoreReviewEvent;
import cn.edu.nju.TomatoMall.service.impl.events.store.StoreStatusChangeEvent;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final SecurityUtil securityUtil;
    private final FileUtil fileUtil;
    private static final List<StoreStatus> AWAITING_REVIEW_STATUS = Arrays.asList(StoreStatus.PENDING, StoreStatus.UPDATING);
    private final EmploymentRepository employmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public StoreServiceImpl(StoreRepository storeRepository,
                            SecurityUtil securityUtil,
                            FileUtil fileUtil,
                            EmploymentRepository employmentRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.storeRepository = storeRepository;
        this.securityUtil = securityUtil;
        this.fileUtil = fileUtil;
        this.employmentRepository = employmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreInfoResponse> getStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        return storeRepository.findAllNormal(pageable).map(StoreInfoResponse::new);
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
        if (store.getStatus().equals(StoreStatus.DELETED)
                || (!store.getStatus().equals(StoreStatus.NORMAL)
                        && !securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)
                        && !securityUtil.getCurrentUser().equals(store.getManager())) // 普通用户只能查看正常店铺的信息
        ) {
            throw TomatoMallException.storeNotFound();
        }

        return new StoreInfoResponse(store);
    }

    @Override
    @Transactional
    public void createStore(String name,
                            String description,
                            MultipartFile logo,
                            String address,
                            List<MultipartFile> qualifications,
                            Map<PaymentMethod, String> merchantAccounts) {
        validateName(name);
        validateLogo(logo);
        validateAddress(address);
        validateDescription(description);
        validateQualifications(qualifications);
        validateMerchantAccounts(merchantAccounts);

        if (storeRepository.existsByName(name)) {
            throw TomatoMallException.storeNameAlreadyExists();
        }

        User user = securityUtil.getCurrentUser();
        Store store = Store.builder()
                .name(name)
                .manager(user)
                .status(StoreStatus.PENDING)
                .logoUrl(fileUtil.upload(user.getId(), logo))
                .address(address)
                .description(description)
                .qualifications(qualifications.stream()
                        .map(qualification -> fileUtil.upload(user.getId(), qualification))
                        .collect(Collectors.toList()))
                .merchantAccounts(merchantAccounts)
                .build();

        storeRepository.save(store);

        // 发布店铺创建事件
        eventPublisher.publishEvent(new StoreStatusChangeEvent(store));
    }

    @Transactional
    @Override
    public void updateStore(int storeId,
                            String name,
                            String description,
                            MultipartFile logo,
                            String address,
                            List<MultipartFile> qualifications,
                            Map<PaymentMethod, String> merchantAccounts) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        validatePermission(store);

        boolean needReview = false;

        if (name != null) {
            validateName(name);
            store.setName(name);
            needReview = true;
        }
        if (logo != null) {
            validateLogo(logo);
            if(store.getLogoUrl() != null){
                fileUtil.delete(store.getLogoUrl());
            }
            store.setLogoUrl(fileUtil.upload(store.getManager().getId(), logo));
            needReview = true;
        }
        if (address != null) {
            validateAddress(address);
            store.setAddress(address);
            needReview = true;
        }
        if (description != null) {
            validateDescription(description);
            store.setDescription(description);
        }
        if (qualifications != null && !qualifications.isEmpty()) {
            validateQualifications(qualifications);
            store.getQualifications().forEach(fileUtil::delete);
            store.setQualifications(qualifications.stream()
                    .map(qualification -> fileUtil.upload(store.getManager().getId(), qualification))
                    .collect(Collectors.toList())
            );
            needReview = true;
        }
        if (merchantAccounts != null) {
            validateMerchantAccounts(merchantAccounts);
            store.setMerchantAccounts(merchantAccounts);
        }

        if (needReview) {
            store.setStatus(StoreStatus.UPDATING);
            // 发布店铺更新事件
            eventPublisher.publishEvent(new StoreStatusChangeEvent(store));
        }

        storeRepository.save(store);
    }

    @Override
    @Transactional
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

        store.setStatus(StoreStatus.DELETED);

        employmentRepository.deleteAllByStoreId(store.getId());

        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void review(int storeId, boolean pass, String comment) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);

        switch (store.getStatus()) {
            case PENDING:
            case UPDATING:
                if (pass) {
                    store.setStatus(StoreStatus.NORMAL);
                    storeRepository.save(store);
                } else {
                    store.setStatus(StoreStatus.SUSPENDED);
                    storeRepository.save(store);
                }
                break;
            case DELETING:
                if (pass) {
                    deleteStore(store);
                } else {
                    store.setStatus(StoreStatus.SUSPENDED);
                    storeRepository.save(store);
                }
                break;
            default: throw TomatoMallException.invalidOperation();
        }

        // 发布审核事件
        eventPublisher.publishEvent(new StoreReviewEvent(store, pass, comment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreInfoResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatusIn(AWAITING_REVIEW_STATUS, pageable);

        return storePage.map(StoreInfoResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreInfoResponse> getSuspendedStoreList(int page, int size, String field, boolean order) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE,
                Sort.by(order ? Sort.Direction.ASC : Sort.Direction.DESC, field));

        Page<Store> storePage = storeRepository.findByStatus(StoreStatus.SUSPENDED, pageable);

        return storePage.map(StoreInfoResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getStoreQualification(int storeId) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        return storeRepository.findById(storeId).orElseThrow(TomatoMallException::unexpectedError).getQualifications();
    }

    @Override
    public Map<PaymentMethod, String> getMerchantAccounts(int storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(TomatoMallException::storeNotFound);
        validatePermission(store);
        return store.getMerchantAccounts();
    }

    private void validatePermission(Store store) {
        if (!store.getManager().equals(securityUtil.getCurrentUser())) {
            throw TomatoMallException.permissionDenied();
        }
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > 50) {
            throw TomatoMallException.invalidParameter("店铺名称不合法");
        }
    }

    private void validateLogo(MultipartFile logo) {
        if (logo == null || logo.isEmpty()) {
            throw TomatoMallException.invalidParameter("店铺logo不能为空");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isEmpty()) {
            throw TomatoMallException.invalidParameter("店铺地址不能为空");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw TomatoMallException.invalidParameter("店铺描述不能为空");
        }
    }

    private void validateQualifications(List<MultipartFile> qualifications) {
        if (qualifications == null || qualifications.isEmpty()) {
            throw TomatoMallException.invalidParameter("店铺资质不能为空");
        }
    }

    private void validateMerchantAccounts(Map<PaymentMethod, String> merchantAccounts) {
        if (merchantAccounts == null || merchantAccounts.isEmpty()) {
            throw TomatoMallException.invalidParameter("商户账号信息不能为空");
        }
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            String account = merchantAccounts.get(paymentMethod);
            if (account == null || account.isEmpty()) {
                throw TomatoMallException.invalidParameter("商户账号信息不完整: " + paymentMethod);
            }
            if (!paymentMethod.isValidAccount(account.trim())) {
                throw TomatoMallException.invalidParameter("商户账号信息不合法: " + paymentMethod + " - " + account);
            }
        }
    }

}
