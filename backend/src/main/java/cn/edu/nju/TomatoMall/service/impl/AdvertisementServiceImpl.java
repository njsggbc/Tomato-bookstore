package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.advertisement.*;
import cn.edu.nju.TomatoMall.models.dto.payment.PaymentInfoResponse;
import cn.edu.nju.TomatoMall.models.po.*;
import cn.edu.nju.TomatoMall.repository.*;
import cn.edu.nju.TomatoMall.service.AdvertisementService;
import cn.edu.nju.TomatoMall.service.PermissionService;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdPlacementCancelEvent;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdvertisingEvent;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdvertisingReviewEvent;
import cn.edu.nju.TomatoMall.service.impl.events.payment.PaymentCreateEvent;
import cn.edu.nju.TomatoMall.service.impl.strategy.AdChargingStrategy;
import cn.edu.nju.TomatoMall.service.impl.strategy.SimpleAdChargingStrategy;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementSpaceRepository advertisementSpaceRepository;
    private final AdvertisementPlacementRepository advertisementPlacementRepository;
    private final StoreRepository storeRepository;
    private final PermissionService permissionService;
    private final FileUtil fileUtil;
    private final SecurityUtil securityUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final AdvertisementSlotRepository advertisementSlotRepository;
    private final AdChargingStrategy adChargingStrategy;
    private final PaymentRepository paymentRepository;

    @Autowired
    public AdvertisementServiceImpl(
            AdvertisementRepository advertisementRepository,
            AdvertisementSpaceRepository advertisementSpaceRepository,
            AdvertisementPlacementRepository advertisementPlacementRepository,
            StoreRepository storeRepository,
            PermissionService permissionService,
            FileUtil fileUtil,
            SecurityUtil securityUtil,
            ApplicationEventPublisher eventPublisher,
            AdvertisementSlotRepository advertisementSlotRepository,
            SimpleAdChargingStrategy adChargingStrategy,
            PaymentRepository paymentRepository) {
        this.advertisementRepository = advertisementRepository;
        this.advertisementSpaceRepository = advertisementSpaceRepository;
        this.advertisementPlacementRepository = advertisementPlacementRepository;
        this.storeRepository = storeRepository;
        this.permissionService = permissionService;
        this.fileUtil = fileUtil;
        this.securityUtil = securityUtil;
        this.eventPublisher = eventPublisher;
        this.advertisementSlotRepository = advertisementSlotRepository;
        this.adChargingStrategy = adChargingStrategy;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // update();
    }

    // --------------------- 广告管理 ---------------------

    @Override
    public List<AdInfoResponse> getStoreAdvertisementList(int storeId) {
        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(storeId);
        if (role != StoreRole.MANAGER && role != StoreRole.STAFF) {
            throw TomatoMallException.permissionDenied();
        }

        List<Advertisement> ads = advertisementRepository.findByStoreId(storeId);
        return ads.stream()
                .map(AdInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public AdInfoResponse getAdvertisement(int adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(TomatoMallException::advertisementNotFound);

        switch (ad.getStatus()) {
            case ENABLED: // 已启用, 所有人可查看
                return new AdInfoResponse(ad);
            case PENDING: // 待审核, 管理员和商店人员可查看
                if (securityUtil.getCurrentUser().getRole() == Role.ADMIN) {
                    return new AdInfoResponse(ad);
                }
            case DISABLED: // 未启用, 只有商店人员可查看
                StoreRole storeRole = permissionService.getStoreRole(ad.getStore().getId());
                if (storeRole == StoreRole.MANAGER || storeRole == StoreRole.STAFF) {
                    return new AdInfoResponse(ad);
                }
            default:
                throw TomatoMallException.advertisementNotFound();
        }
    }

    @Transactional
    @Override
    public void createAdvertisement(int storeId, String title, MultipartFile content, String linkUrl) {
        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(storeId);
        if (role != StoreRole.MANAGER) {
            throw TomatoMallException.permissionDenied();
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(TomatoMallException::storeNotFound);

        Advertisement ad = Advertisement.builder()
                .title(title)
                .content(fileUtil.upload(securityUtil.getCurrentUser().getId(), content))
                .linkUrl(linkUrl)
                .store(store)
                .build();

        advertisementRepository.save(ad);
    }

    @Transactional
    @Override
    public void updateAdvertisement(int adId, String title, MultipartFile content, String linkUrl) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(TomatoMallException::advertisementNotFound);

        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(ad.getStore().getId());
        if (role != StoreRole.MANAGER) {
            throw TomatoMallException.permissionDenied();
        }

        if (ad.getStatus() != AdStatus.DISABLED) {
            throw TomatoMallException.invalidOperation("当前广告状态不允许修改");
        }

        if (title != null && !title.isEmpty()) {
            ad.setTitle(title);
        }

        if (linkUrl != null && !linkUrl.isEmpty()) {
            ad.setLinkUrl(linkUrl);
        }

        if (content != null && !content.isEmpty()) {
            fileUtil.delete(ad.getContent());
            String imageUrl = fileUtil.upload(securityUtil.getCurrentUser().getId(), content);
            ad.setContent(imageUrl);
        }

        advertisementRepository.save(ad);
    }

    @Transactional
    @Override
    public void deleteAdvertisement(int adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(TomatoMallException::advertisementNotFound);

        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(ad.getStore().getId());
        if (role != StoreRole.MANAGER) {
            throw TomatoMallException.permissionDenied();
        }

        if (ad.getStatus() != AdStatus.DISABLED) {
            throw TomatoMallException.invalidOperation("该广告仍有活跃的投放记录，无法删除");
        }

        fileUtil.delete(ad.getContent());
        advertisementRepository.delete(ad);
    }

    // --------------------- 广告展示 ---------------------

    @Override
    public List<AdInfoResponse> getAdvertisementList(AdSpaceType adSpaceType) {
        List<AdvertisementSpace> spaces;
        if (adSpaceType == null) {
            spaces = advertisementSpaceRepository.findAll();
        } else {
            spaces = advertisementSpaceRepository.findByType(adSpaceType);
        }

        // 获取当前启用的广告
        return spaces.stream()
                .map(space -> {
                    AdvertisementSlot slot = space.getCurrent();
                    return slot.isActive() ? slot.getAdvertisement() : null;
                })
                .filter(Objects::nonNull)
                .map(AdInfoResponse::new)
                .collect(Collectors.toList());
    }

    // --------------------- 广告投放 ---------------------

    @Transactional
    @Override
    public PaymentInfoResponse deliverAdvertisement(int adId, int spaceId, List<Integer> adSlotIds) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(TomatoMallException::advertisementNotFound);

        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(ad.getStore().getId());
        if (role != StoreRole.MANAGER) {
            throw TomatoMallException.permissionDenied();
        }

        AdvertisementSpace space = advertisementSpaceRepository.findById(spaceId)
                .orElseThrow(TomatoMallException::adSpaceNotFound);

        List<AdvertisementSlot> slots = advertisementSlotRepository.findAllById(adSlotIds);

        if (slots.isEmpty() || slots.stream().map(AdvertisementSlot::getSpace).distinct().count() > 1) {
            throw TomatoMallException.invalidOperation("无效时间段");
        }

        if (slots.stream().anyMatch(slot -> !slot.isAvailable())) {
            throw TomatoMallException.invalidOperation("当前时间段不可用");
        }

        List<LocalDateTime> displayTimeList = new ArrayList<>();

        for (AdvertisementSlot slot : slots) {
            slot.setAdvertisement(ad);
            slot.setAvailable(false);
            slot.setActive(false);
            displayTimeList.add(slot.getStartTime());
        }

        // 创建广告投放记录
        AdvertisementPlacement placement = AdvertisementPlacement.builder()
                .advertisement(ad)
                .space(space)
                .displayTimeList(displayTimeList)
                .displayDuration(space.getSegment())
                .slotList(slots)
                .build();

        // 保存投放记录
        advertisementPlacementRepository.save(placement);

        // 更新广告状态
        updateAdStatus(ad);

        // 发布投放事件
        eventPublisher.publishEvent(new AdvertisingEvent(placement));

        // 创建支付
        Payment payment = adChargingStrategy.charge(placement);
        payment = paymentRepository.save(payment);

        // 发布支付创建事件
        eventPublisher.publishEvent(new PaymentCreateEvent(payment));

        return new PaymentInfoResponse(payment);
    }

    @Transactional
    @Override
    public void cancelDeliverAdvertisementInternal(int placementId) {
        AdvertisementPlacement placement = advertisementPlacementRepository.findById(placementId)
                .orElseThrow(TomatoMallException::adPlacementNotFound);

        cancelDeliverAdvertisementInternal(placement);
    }

    @Transactional
    @Override
    public void cancelDeliverAdvertisement(int placementId) {
        AdvertisementPlacement placement = advertisementPlacementRepository.findById(placementId)
                .orElseThrow(TomatoMallException::adPlacementNotFound);

        Advertisement ad = placement.getAdvertisement();

        // 验证当前用户对广告所属商店的权限
        StoreRole role = permissionService.getStoreRole(ad.getStore().getId());
        if (role != StoreRole.MANAGER) {
            throw TomatoMallException.permissionDenied();
        }

        cancelDeliverAdvertisementInternal(placement);
    }

    private void cancelDeliverAdvertisementInternal(AdvertisementPlacement placement) {
        Advertisement ad = placement.getAdvertisement();
        AdvertisementSpace space = placement.getSpace();

        // 只有待审核和启用状态的投放才能取消
        if (placement.getStatus() != AdPlacementStatus.PENDING &&
                placement.getStatus() != AdPlacementStatus.ENABLED) {
            throw TomatoMallException.invalidOperation("无法取消该状态的投放");
        }

        // 检查是否有时间点在12小时内
        LocalDateTime minimumCancelTime = LocalDateTime.now().plusHours(12);
        if (placement.getDisplayTimeList().stream()
                .anyMatch(time -> time.isBefore(minimumCancelTime))) {
            throw TomatoMallException.invalidOperation("无法取消12小时内的投放");
        }

        // 清除广告槽位
        LocalDateTime minimumAvailableTime = LocalDateTime.now().plusHours(24);
        for (AdvertisementSlot slot : placement.getSlotList()) {
            slot.setAdvertisement(null);
            slot.setAvailable(slot.getStartTime().isAfter(minimumAvailableTime));
            slot.setActive(false);
        }
        advertisementSpaceRepository.save(space);

        // 标记投放为已取消
        placement.setStatus(AdPlacementStatus.DISABLED);
        advertisementPlacementRepository.save(placement);

        // 更新广告状态
        updateAdStatus(ad);

        // 发布取消投放事件
        eventPublisher.publishEvent(new AdPlacementCancelEvent(placement));
    }

    @Transactional
    @Override
    public void reviewAdvertisementPlacement(int placementId, boolean isPass, String comment) {
        // 检查当前用户是否为管理员
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        AdvertisementPlacement placement = advertisementPlacementRepository.findById(placementId)
                .orElseThrow(TomatoMallException::adPlacementNotFound);

        // 只有待审核状态的投放才能审核
        if (placement.getStatus() != AdPlacementStatus.PENDING) {
            throw TomatoMallException.invalidOperation("只能审核待审核状态的投放");
        }

        // 更新投放状态
        placement.setStatus(isPass ? AdPlacementStatus.ENABLED : AdPlacementStatus.DISABLED);
        advertisementPlacementRepository.save(placement);

        // 更新槽位状态
        LocalDateTime minimumAvailableTime = LocalDateTime.now().plusHours(24);
        if (isPass){
            placement.getSlotList().forEach(slot ->
                slot.setActive(true)
            );
        } else {
            placement.getSlotList().forEach(slot -> {
                slot.setAdvertisement(null);
                slot.setActive(false);
                slot.setAvailable(slot.getStartTime().isAfter(minimumAvailableTime));
            });
        }

        placement.setComment(comment);

        advertisementSlotRepository.saveAll(placement.getSlotList());

        // 更新广告状态
        updateAdStatus(placement.getAdvertisement());

        // 发布审核事件
        eventPublisher.publishEvent(new AdvertisingReviewEvent(placement, isPass, comment));
    }

    // --------------------- 广告位管理 ---------------------

    @Override
    public List<AdSpaceInfoResponse> getAdSpaceList(AdSpaceType adSpaceType) {
        List<AdvertisementSpace> spaces;

        if (adSpaceType == null) {
            spaces = advertisementSpaceRepository.findAll();
        } else {
            spaces = advertisementSpaceRepository.findByType(adSpaceType);
        }

        return spaces.stream()
                .map(AdSpaceInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdSlotInfoResponse> getAdSlot(int adSpaceId) {
        return advertisementSpaceRepository.findById(adSpaceId)
                .orElseThrow(TomatoMallException::adSpaceNotFound)
                .getSlots()
                .stream()
                .map(AdSlotInfoResponse::new)
                .sorted(Comparator.comparing(AdSlotInfoResponse::getStartTime))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void createAdSpace(String label, AdSpaceType type, int cycleInDay, int segmentInHour) {
        // 检查当前用户是否为管理员
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        // 检查标签是否已存在
        if (advertisementSpaceRepository.findByLabel(label).isPresent()) {
            throw TomatoMallException.labelAlreadyExists();
        }

        // 创建广告位
        AdvertisementSpace space = AdvertisementSpace.builder()
                .label(label)
                .type(type)
                .cycle(cycleInDay)
                .segment(segmentInHour)
                .build();

        // 创建槽位
        int size = space.getCycle() * 24 / space.getSegment();
        List<AdvertisementSlot> slots = new ArrayList<>(size);

        // 设置初始时间
        LocalDateTime startTime = LocalDateTime.now()
                .truncatedTo(ChronoUnit.HOURS)
                .minusHours(LocalDateTime.now().getHour() % space.getSegment());

        // 初始化槽位
        for (int i = 0; i < size; i++) {
            slots.add(
                    AdvertisementSlot.builder()
                            .space(space)
                            .startTime(startTime)
                            .build()
            );
            startTime = startTime.plusHours(space.getSegment());
        }

        // 构建双向环形链表
        AdvertisementSlot prev = null;
        for (int i = 0; i < slots.size(); i++) {
            AdvertisementSlot curr = slots.get(i);
            AdvertisementSlot next = slots.get((i + 1) % slots.size());
            curr.setPrev(prev);
            curr.setNext(next);
            prev = curr;
        }

        // 将前24小时内的槽位设为不可用
        for (int i = 0; i < 24 / space.getSegment(); i++) {
            slots.get(i).setAvailable(false);
        }

        space.setSlots(slots);
        space.setCurrent(slots.get(0));
        space.setForbiddenEdge(slots.get(24 / segmentInHour));

        advertisementSpaceRepository.save(space);
    }

    @Transactional
    @Override
    public void setAdSlotStatus(Integer spaceId, List<Integer> slotIds, Boolean available, Boolean active) {
        // 检查当前用户是否为管理员
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        if (available == null && active == null) {
            throw TomatoMallException.invalidOperation();
        }

        List<AdvertisementSlot> slots;

        if (spaceId != null) {
            slots = advertisementSpaceRepository.findById(spaceId)
                    .orElseThrow(TomatoMallException::adSpaceNotFound)
                    .getSlots();
        } else {
            slots = advertisementSlotRepository.findAllById(slotIds);
        }

        if (slots.isEmpty()) {
            throw TomatoMallException.invalidOperation("没有可以设置的时间段");
        }

        if (available != null) {
            advertisementSlotRepository.findAllById(slotIds)
                    .forEach(adSlot -> adSlot.setAvailable(available));
        }

        if (active != null) {
            advertisementSlotRepository.findAllById(slotIds)
                    .forEach(adSlot -> adSlot.setActive(active));
        }
    }

    @Transactional
    @Override
    public void deleteAdSpace(int adSpaceId) {
        // 检查当前用户是否为管理员
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        AdvertisementSpace space = advertisementSpaceRepository.findById(adSpaceId)
                .orElseThrow(TomatoMallException::adSpaceNotFound);

        // 检查是否有广告在使用该广告位
        if (space.getSlots().stream().map(AdvertisementSlot::getAdvertisement).anyMatch(Objects::nonNull)) {
            throw TomatoMallException.invalidOperation("该广告位仍在使用中，无法删除");
        }

        advertisementSpaceRepository.delete(space);
    }

    // --------------------- 广告投放记录管理 ---------------------

    @Override
    public List<AdPlacementInfoResponse> getStorePlacementList(int storeId) {
        // 验证当前用户对该商店的权限
        StoreRole role = permissionService.getStoreRole(storeId);
        if (role != StoreRole.MANAGER && role != StoreRole.STAFF) {
            throw TomatoMallException.permissionDenied();
        }

        List<AdvertisementPlacement> placements = advertisementPlacementRepository.findByAdvertisementStoreId(storeId);
        return placements.stream()
                .map(AdPlacementInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdPlacementInfoResponse> getAdSpacePlacementList(int adSpaceId) {
        // 只有管理员可以查看特定广告位的所有投放
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        List<AdvertisementPlacement> placements = advertisementPlacementRepository.findBySpaceId(adSpaceId);
        return placements.stream()
                .map(AdPlacementInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdPlacementInfoResponse> getAllPendingPlacements() {
        // 只有管理员可以查看所有待审核的投放
        if (securityUtil.getCurrentUser().getRole() != Role.ADMIN) {
            throw TomatoMallException.permissionDenied();
        }

        List<AdvertisementPlacement> placements = advertisementPlacementRepository.findByStatus(AdPlacementStatus.PENDING);
        return placements.stream()
                .map(AdPlacementInfoResponse::new)
                .collect(Collectors.toList());
    }

    // --------------------- 定时更新 ---------------------

    @Scheduled(cron = "0 0 0/3 * * ?")
    @Transactional
    public void update() {
        List<AdvertisementSpace> spaces = advertisementSpaceRepository.findAll();
        for (AdvertisementSpace space : spaces) {
            refreshSpace(space);
        }
    }

    @Transactional
    public void refreshSpace(AdvertisementSpace space) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentSlotEndTime = space.getCurrent().getStartTime().plusHours(space.getSegment());

        // 如果当前时间超过了当前槽位的结束时间，需要进行多次tick直到追上当前时间
        while (now.isAfter(currentSlotEndTime)) {
            Advertisement expiredAd = space.tick();
            updateAdStatus(expiredAd);
            currentSlotEndTime = space.getCurrent().getStartTime().plusHours(space.getSegment());
        }
    }

    @Transactional
    public void updateAdStatus(Advertisement ad) {
        if (advertisementSlotRepository.existsByAdvertisementIdAndActive(ad.getId(), true)) {
            ad.setStatus(AdStatus.ENABLED);
        } else if (advertisementSlotRepository.existsByAdvertisementIdAndActive(ad.getId(), false)) {
            ad.setStatus(AdStatus.PENDING);
        } else {
            ad.setStatus(AdStatus.DISABLED);
        }
        advertisementRepository.save(ad);
    }

}