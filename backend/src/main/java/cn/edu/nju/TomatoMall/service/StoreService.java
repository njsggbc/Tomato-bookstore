package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.store.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StoreService {
    Page<StoreInfoResponse> getStoreList(int page, int size, String field, boolean order);

    List<StoreInfoResponse> getManagedStoreList();

    List<StoreInfoResponse> getWorkedStoreList();

    StoreInfoResponse getInfo(int storeId);

    void createStore(String name, String description, MultipartFile logo, String address, List<MultipartFile> qualifications);

    void updateStore(int storeId, String name, String description, MultipartFile logo, String address, List<MultipartFile> qualifications);

    void deleteStore(int storeId);

    void review(int storeId, boolean pass);

    Page<StoreInfoResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order);

    Page<StoreInfoResponse> getSuspendedStoreList(int page, int size, String field, boolean order);

    List<String> getStoreQualification(int storeId);
}
