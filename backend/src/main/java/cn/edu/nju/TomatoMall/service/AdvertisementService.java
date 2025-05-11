package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdvertisementService {

    List<AdInfoResponse> getStoreAdvertisementList(int storeId);

    AdInfoResponse getAdvertisement(int adId);

    @Transactional
    void createAdvertisement(AdCreateRequest params);

    @Transactional
    void updateAdvertisement(int adId, AdUpdateRequest params);

    @Transactional
    void deleteAdvertisement(int adId);

    List<AdInfoResponse> getAdvertisementList(AdSpaceType adSpaceType);

    List<AdSlotInfoResponse> getAdSlot(int adSpaceId);

    @Transactional
    void deliverAdvertisement(int adId, int adSpaceId, List<Integer> slotIds);

    @Transactional
    void cancelDeliverAdvertisement(int placementId);

    @Transactional
    void reviewAdvertisementPlacement(int placementId, boolean isPass);

    List<AdSpaceInfoResponse> getAdSpaceList(AdSpaceType adSpaceType);

    @Transactional
    void createAdSpace(AdSpaceCreateRequest params);

    @Transactional
    void setAdSlotStatus(Integer spaceId, List<Integer> slotIds, Boolean available, Boolean active);

    @Transactional
    void deleteAdSpace(int adSpaceId);

    List<AdPlacementInfoResponse> getStorePlacementList(int storeId);

    List<AdPlacementInfoResponse> getAdSpacePlacementList(int adSpaceId);

    List<AdPlacementInfoResponse> getAllPendingPlacements();
}
