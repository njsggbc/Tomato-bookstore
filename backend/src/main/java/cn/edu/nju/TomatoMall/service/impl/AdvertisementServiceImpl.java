package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.advertisements.AddAdvertisementRequest;
import cn.edu.nju.TomatoMall.models.dto.advertisements.AdvertisementBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.advertisements.UpdateAdvertisementRequest;
import cn.edu.nju.TomatoMall.models.po.Advertisement;
import cn.edu.nju.TomatoMall.models.po.Product;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.AdvertisementRepository;
import cn.edu.nju.TomatoMall.repository.ProductRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.AdvertisementService;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    StoreRepository storeRepository;

    @Autowired
    AdvertisementRepository advertisementRepository;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    FileUtil fileUtil;

    @Override
    public Page<AdvertisementBriefResponse> getAdvertisementList(int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        Page<Advertisement> advertisementPage = advertisementRepository.findAll(pageable);

        return advertisementPage.map(AdvertisementBriefResponse::new);
    }

    @Override
    public Boolean createAdvertisement(AddAdvertisementRequest params) {
        Product product = productRepository.findById(params.getProductId()).orElseThrow(TomatoMallException::productNotFound);
        Store store = product.getStore();
        User user = securityUtil.getCurrentUser();
        if (!store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(params.getTitle());
        advertisement.setContent(params.getContent());
        advertisement.setImageUrls(params.getImgUrls().stream()
                .map(image -> fileUtil.upload(user.getId(), image))
                .collect(Collectors.toList())
        );
        advertisement.setProductId(params.getProductId());
        advertisement.setCreateTime(LocalDateTime.now());

        // 主实体级联保存
        product.getAdvertisements().add(advertisement);
        productRepository.save(product);

        return true;
    }

    @Override
    public Boolean updateAdvertisement(int advertisementId, UpdateAdvertisementRequest params) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId).orElseThrow(TomatoMallException::advertisementNotFound);
        Product product = advertisement.getProduct();
        Store store = product.getStore();
        User user = securityUtil.getCurrentUser();
        if (!store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        if (params.getTitle() != null) {
            advertisement.setTitle(params.getTitle());
        }
        if (params.getContent() != null) {
            advertisement.setContent(params.getContent());
        }
        if (params.getImageUrls() != null) {
            if (advertisement.getImageUrls() != null) {
                advertisement.getImageUrls().forEach(fileUtil::delete);
            }
            advertisement.setImageUrls(params.getImageUrls().stream()
                    .map(image -> fileUtil.upload(user.getId(), image))
                    .collect(Collectors.toList())
            );
        }
        if (params.getProductId() != null) {
            advertisement.setProductId(params.getProductId());
        }

        advertisementRepository.save(advertisement);

        return true;
    }

    @Override
    public String deleteAdvertisement(int advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId).orElseThrow(TomatoMallException::advertisementNotFound);
        Product product = advertisement.getProduct();
        Store store = product.getStore();
        User user = securityUtil.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        if (advertisement.getImageUrls() != null) {
            advertisement.getImageUrls().forEach(fileUtil::delete);
        }

        // 主实体级联保存
        product.getAdvertisements().remove(advertisement);
        storeRepository.save(store);

        return "删除成功";
    }
}
