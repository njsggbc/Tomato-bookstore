package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.store.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StoreService {
    Page<StoreInfoResponse> getStoreList(int page, int size, String field, boolean order);

    List<StoreInfoResponse> getManagedStoreList();

    List<StoreInfoResponse> getWorkedStoreList();

    StoreInfoResponse getInfo(int storeId);

    void createStore(StoreCreateRequest params);

    void updateStore(int storeId, StoreUpdateRequest params);

    void deleteStore(int storeId);

    void review(int storeId, boolean pass);

    Page<StoreInfoResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order);

    Page<StoreInfoResponse> getSuspendedStoreList(int page, int size, String field, boolean order);

    List<String> getStoreQualification(int storeId);
}
