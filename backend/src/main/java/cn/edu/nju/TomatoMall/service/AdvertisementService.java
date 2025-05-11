package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdvertisementService {

    List<AdInfoResponse> getStoreAdvertisementList(int storeId);

    AdInfoResponse getAdvertisement(int adId);

    @Transactional
    void createAdvertisement(int storeId, String title, MultipartFile content, String linkUrl);

    @Transactional
    void updateAdvertisement(int adId, String title, MultipartFile content, String linkUrl);

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
    void createAdSpace(String label, AdSpaceType adSpaceType, int cycleInDay, int segmentInHour);

    @Transactional
    void setAdSlotStatus(Integer spaceId, List<Integer> slotIds, Boolean available, Boolean active);

    @Transactional
    void deleteAdSpace(int adSpaceId);

    List<AdPlacementInfoResponse> getStorePlacementList(int storeId);

    List<AdPlacementInfoResponse> getAdSpacePlacementList(int adSpaceId);

    List<AdPlacementInfoResponse> getAllPendingPlacements();
}
