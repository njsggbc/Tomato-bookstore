package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.advertisements.AdvertisementBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.advertisements.AddAdvertisementRequest;
import cn.edu.nju.TomatoMall.models.dto.advertisements.UpdateAdvertisementRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface AdvertisementService {

    // 获取广告列表
    Page<AdvertisementBriefResponse> getAdvertisementList(int page, int size, String field, boolean order);

    // 创建广告
    Boolean createAdvertisement(AddAdvertisementRequest params);

    // 更新广告
    Boolean updateAdvertisement(int advertisementId, UpdateAdvertisementRequest params);

    // 删除广告
    String deleteAdvertisement(int advertisementId);

}


