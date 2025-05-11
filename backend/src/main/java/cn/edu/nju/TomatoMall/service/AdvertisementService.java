package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 广告管理服务接口
 */
public interface AdvertisementService {

    /**
     * 获取商店的广告列表
     * @param storeId 商店ID
     * @return 广告信息列表
     */
    List<AdInfoResponse> getStoreAdvertisementList(int storeId);

    /**
     * 获取特定广告信息
     * @param adId 广告ID
     * @return 广告信息
     */
    AdInfoResponse getAdvertisement(int adId);

    /**
     * 创建新广告
     * @param storeId 商店ID
     * @param title 广告标题
     * @param content 广告内容
     * @param linkUrl 链接URL
     */
    @Transactional
    void createAdvertisement(int storeId, String title, MultipartFile content, String linkUrl);

    /**
     * 更新广告信息
     * @param adId 广告ID
     * @param title 广告标题
     * @param content 广告内容
     * @param linkUrl 链接URL
     */
    @Transactional
    void updateAdvertisement(int adId, String title, MultipartFile content, String linkUrl);

    /**
     * 删除广告
     * @param adId 广告ID
     */
    @Transactional
    void deleteAdvertisement(int adId);

    /**
     * 获取特定类型广告位的广告列表
     * @param adSpaceType 广告位类型
     * @return 广告信息列表
     */
    List<AdInfoResponse> getAdvertisementList(AdSpaceType adSpaceType);

    /**
     * 获取广告位的可用槽位信息
     * @param adSpaceId 广告位ID
     * @return 槽位信息列表
     */
    List<AdSlotInfoResponse> getAdSlot(int adSpaceId);

    /**
     * 投放广告
     * @param adId 广告ID
     * @param adSpaceId 广告位ID
     * @param slotIds 槽位ID列表
     */
    @Transactional
    void deliverAdvertisement(int adId, int adSpaceId, List<Integer> slotIds);

    /**
     * 取消广告投放
     * @param placementId 投放ID
     */
    @Transactional
    void cancelDeliverAdvertisement(int placementId);

    /**
     * 审核广告投放
     * @param placementId 投放ID
     * @param isPass 是否通过
     */
    @Transactional
    void reviewAdvertisementPlacement(int placementId, boolean isPass);

    /**
     * 获取广告位列表
     * @param adSpaceType 广告位类型
     * @return 广告位信息列表
     */
    List<AdSpaceInfoResponse> getAdSpaceList(AdSpaceType adSpaceType);

    /**
     * 创建广告位
     * @param label 标签
     * @param adSpaceType 广告位类型
     * @param cycleInDay 周期（天）
     * @param segmentInHour 时间段（小时）
     */
    @Transactional
    void createAdSpace(String label, AdSpaceType adSpaceType, int cycleInDay, int segmentInHour);

    /**
     * 设置广告槽位状态
     * @param spaceId 广告位ID
     * @param slotIds 槽位ID列表
     * @param available 是否可用
     * @param active 是否激活
     */
    @Transactional
    void setAdSlotStatus(Integer spaceId, List<Integer> slotIds, Boolean available, Boolean active);

    /**
     * 删除广告位
     * @param adSpaceId 广告位ID
     */
    @Transactional
    void deleteAdSpace(int adSpaceId);

    /**
     * 获取商店的广告投放记录
     * @param storeId 商店ID
     * @return 投放记录列表
     */
    List<AdPlacementInfoResponse> getStorePlacementList(int storeId);

    /**
     * 获取广告位的广告投放记录
     * @param adSpaceId 广告位ID
     * @return 投放记录列表
     */
    List<AdPlacementInfoResponse> getAdSpacePlacementList(int adSpaceId);

    /**
     * 获取所有待审核的广告投放记录
     * @return 投放记录列表
     */
    List<AdPlacementInfoResponse> getAllPendingPlacements();
}
