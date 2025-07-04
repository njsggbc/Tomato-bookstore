package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * advertisement
 */
@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    // --------------------- 广告管理 ---------------------

    /**
     * 获取商店的广告列表
     */
    @GetMapping("/store/{storeId}")
    public ApiResponse<List<AdInfoResponse>> getStoreAdvertisementList(@PathVariable("storeId") int storeId) {
        return ApiResponse.success(advertisementService.getStoreAdvertisementList(storeId));
    }

    /**
     * 获取特定广告信息
     */
    @GetMapping("/{adId}")
    public ApiResponse<AdInfoResponse> getAdvertisement(@PathVariable("adId") int adId) {
        return ApiResponse.success(advertisementService.getAdvertisement(adId));
    }

    /**
     * 创建新广告
     */
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<Void> createAdvertisement(@Valid @ModelAttribute AdCreateRequest params) {
        advertisementService.createAdvertisement(
                params.getStoreId(),
                params.getTitle(),
                params.getContent(),
                params.getLinkUrl());
        return ApiResponse.success();
    }

    /**
     * 更新广告信息
     */
    @PutMapping(path = "/{adId}", consumes = "multipart/form-data")
    public ApiResponse<Void> updateAdvertisement(
            @PathVariable("adId") int adId,
            @Valid @ModelAttribute AdUpdateRequest params) {
        advertisementService.updateAdvertisement(
                adId,
                params.getTitle(),
                params.getContent(),
                params.getLinkUrl());
        return ApiResponse.success();
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{adId}")
    public ApiResponse<Void> deleteAdvertisement(@PathVariable("adId") int adId) {
        advertisementService.deleteAdvertisement(adId);
        return ApiResponse.success();
    }

    // --------------------- 广告展示 ---------------------

    /**
     * 获取特定类型广告位的广告列表
     */
    @GetMapping("/display")
    public ApiResponse<List<AdInfoResponse>> getAdvertisementList(
            @RequestParam(value = "type", required = false) AdSpaceType adSpaceType) {
        return ApiResponse.success(advertisementService.getAdvertisementList(adSpaceType));
    }

    // --------------------- 广告投放 ---------------------

    /**
     * 获取广告位列表
     */
    @GetMapping("/spaces")
    public ApiResponse<List<AdSpaceInfoResponse>> getAdSpaceList(
            @RequestParam(value = "type", required = false) AdSpaceType adSpaceType) {
        return ApiResponse.success(advertisementService.getAdSpaceList(adSpaceType));
    }

    /**
     * 获取广告位的可用槽位信息
     */
    @GetMapping("/spaces/{spaceId}/slots")
    public ApiResponse<List<AdSlotInfoResponse>> getAdSlot(@PathVariable("spaceId") int spaceId) {
        return ApiResponse.success(advertisementService.getAdSlot(spaceId));
    }

    /**
     * 创建广告位
     */
    @PostMapping("/spaces")
    public ApiResponse<Void> createAdSpace(@Valid @RequestBody AdSpaceCreateRequest params) {
        advertisementService.createAdSpace(
                params.getLabel(),
                params.getType(),
                params.getCycleInDay(),
                params.getSegmentInHour());
        return ApiResponse.success();
    }

    /**
     * 设置广告槽位状态
     */
    @PatchMapping("/spaces/{spaceId}/slots/status")
    public ApiResponse<Void> setAdSlotStatus(
            @PathVariable("spaceId") int spaceId,
            @Valid @RequestBody AdSlotStatusUpdateRequest params
    ) {
        advertisementService.setAdSlotStatus(spaceId, params.getSlotIds(), params.getAvailable(), params.getActive());
        return ApiResponse.success();
    }

    /**
     * 删除广告位
     */
    @DeleteMapping("spaces/{spaceId}")
    public ApiResponse<Void> deleteAdSpace(@PathVariable("spaceId") int spaceId) {
        advertisementService.deleteAdSpace(spaceId);
        return ApiResponse.success();
    }

    /**
     * 投放广告
     */
    @PostMapping("/placements")
    public ApiResponse<PaymentInfoResponse> deliverAdvertisement(
            @Valid @RequestBody AdPlacementRequest params) {
        return ApiResponse.success(
                advertisementService.deliverAdvertisement(
                params.getAdId(),
                params.getAdSpaceId(),
                params.getAdSlotIds()
        ));
    }

    /**
     * 取消广告投放
     */
    @DeleteMapping("/placements/{placementId}")
    public ApiResponse<Void> cancelDeliverAdvertisement(@PathVariable("placementId") int placementId) {
        advertisementService.cancelDeliverAdvertisement(placementId);
        return ApiResponse.success();
    }

    /**
     * 审核广告投放
     */
    @PatchMapping("/placements/{placementId}/review")
    public ApiResponse<Void> reviewAdvertisementPlacement(
            @PathVariable("placementId") int placementId,
            @RequestParam("pass") boolean pass,
            @RequestBody String comment
           ) {
        advertisementService.reviewAdvertisementPlacement(placementId, pass, comment);
        return ApiResponse.success();
    }

    /**
     * 获取商店的广告投放记录
     */
    @GetMapping("/placements/store/{storeId}")
    public ApiResponse<List<AdPlacementInfoResponse>> getStorePlacementList(@PathVariable("storeId") int storeId) {
        return ApiResponse.success(advertisementService.getStorePlacementList(storeId));
    }

    /**
     * 获取广告位的广告投放记录
     */
    @GetMapping("/placements/space/{adSpaceId}")
    public ApiResponse<List<AdPlacementInfoResponse>> getAdSpacePlacementList(@PathVariable("adSpaceId") int adSpaceId) {
        return ApiResponse.success(advertisementService.getAdSpacePlacementList(adSpaceId));
    }

    /**
     * 获取所有待审核的广告投放记录
     */
    @GetMapping("/placements/pending")
    public ApiResponse<List<AdPlacementInfoResponse>> getAllPendingPlacements() {
        return ApiResponse.success(advertisementService.getAllPendingPlacements());
    }
}