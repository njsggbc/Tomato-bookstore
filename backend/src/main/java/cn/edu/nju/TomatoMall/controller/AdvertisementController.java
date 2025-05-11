package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告管理相关接口
 */
@RestController
@RequestMapping("/api/advertisement")
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
    public ApiResponse<Void> createAdvertisement(@ModelAttribute AdCreateRequest params) {
        advertisementService.createAdvertisement(params);
        return ApiResponse.success();
    }

    /**
     * 更新广告信息
     */
    @PutMapping(path = "/{adId}", consumes = "multipart/form-data")
    public ApiResponse<Void> updateAdvertisement(
            @PathVariable("adId") int adId,
            @ModelAttribute AdUpdateRequest params) {
        advertisementService.updateAdvertisement(adId, params);
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
     * 获取广告位的可用槽位信息
     */
    @GetMapping("/space/{adSpaceId}/slot")
    public ApiResponse<List<AdSlotInfoResponse>> getAdSlot(@PathVariable("adSpaceId") int adSpaceId) {
        return ApiResponse.success(advertisementService.getAdSlot(adSpaceId));
    }

    /**
     * 投放广告
     */
    @PostMapping("/{adId}/placements/{adSpaceId}")
    public ApiResponse<Void> deliverAdvertisement(
            @PathVariable("adId") int adId,
            @PathVariable("adSpaceId") int adSpaceId,
            @RequestBody List<Integer> slotIds) {
        advertisementService.deliverAdvertisement(adId, adSpaceId, slotIds);
        return ApiResponse.success();
    }

    /**
     * 取消广告投放
     */
    @DeleteMapping("/placement/{placementId}")
    public ApiResponse<Void> cancelDeliverAdvertisement(@PathVariable("placementId") int placementId) {
        advertisementService.cancelDeliverAdvertisement(placementId);
        return ApiResponse.success();
    }

    /**
     * 审核广告投放
     */
    @PatchMapping("/placement/{placementId}/review")
    public ApiResponse<Void> reviewAdvertisementPlacement(
            @PathVariable("placementId") int placementId,
            @RequestParam("pass") boolean isPass) {
        advertisementService.reviewAdvertisementPlacement(placementId, isPass);
        return ApiResponse.success();
    }

    // --------------------- 广告投放记录管理 ---------------------

    /**
     * 获取商店的广告投放记录
     */
    @GetMapping("/placement/store/{storeId}")
    public ApiResponse<List<AdPlacementInfoResponse>> getStorePlacementList(@PathVariable("storeId") int storeId) {
        return ApiResponse.success(advertisementService.getStorePlacementList(storeId));
    }

    /**
     * 获取广告位的广告投放记录
     */
    @GetMapping("/placement/space/{adSpaceId}")
    public ApiResponse<List<AdPlacementInfoResponse>> getAdSpacePlacementList(@PathVariable("adSpaceId") int adSpaceId) {
        return ApiResponse.success(advertisementService.getAdSpacePlacementList(adSpaceId));
    }

    /**
     * 获取所有待审核的广告投放记录
     */
    @GetMapping("/placement/pending")
    public ApiResponse<List<AdPlacementInfoResponse>> getAllPendingPlacements() {
        return ApiResponse.success(advertisementService.getAllPendingPlacements());
    }

    /**
     * 获取广告位列表
     */
    @GetMapping("/space")
    public ApiResponse<List<AdSpaceInfoResponse>> getAdSpaceList(
            @RequestParam(value = "type", required = false) AdSpaceType adSpaceType) {
        return ApiResponse.success(advertisementService.getAdSpaceList(adSpaceType));
    }

    /**
     * 创建广告位
     */
    @PostMapping("/space")
    public ApiResponse<Void> createAdSpace(@RequestBody AdSpaceCreateRequest params) {
        advertisementService.createAdSpace(params);
        return ApiResponse.success();
    }

    /**
     * 设置广告槽位状态
     */
    @PatchMapping("/space/slot/status")
    public ApiResponse<Void> setAdSlotStatus(@RequestBody AdSlotStatusUpdateRequest params) {
        advertisementService.setAdSlotStatus(params.getSpaceId(), params.getSlotIds(), params.getAvailable(), params.getActive());
        return ApiResponse.success();
    }

    /**
     * 删除广告位
     */
    @DeleteMapping("space/{adSpaceId}")
    public ApiResponse<Void> deleteAdSpace(@PathVariable("adSpaceId") int adSpaceId) {
        advertisementService.deleteAdSpace(adSpaceId);
        return ApiResponse.success();
    }
}