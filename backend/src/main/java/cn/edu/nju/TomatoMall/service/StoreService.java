package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.store.StoreCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.store.StoreBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StoreService {
    Page<StoreBriefResponse> getStoreList(int page, int size, String field, boolean order);

    StoreDetailResponse getDetail(int storeId);

    StoreBriefResponse getBrief(int storeId);

    Boolean createStore(StoreCreateRequest params);

    Boolean updateStore(int storeId, StoreUpdateRequest params);

    Boolean deleteStore(int storeId);

    List<String> getStoreTokenList(int storeId);

    Boolean generateToken(int storeId);

    Boolean deleteToken(int storeId, String token);

    Boolean authToken(int storeId, String token);

    Boolean deleteStaff(int storeId, int userId);

    Boolean review(int storeId, boolean pass);

    Page<StoreBriefResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order);

    Page<StoreBriefResponse> getSuspendedStoreList(int page, int size, String field, boolean order);

    List<String> getStoreQualification(int storeId);
}
