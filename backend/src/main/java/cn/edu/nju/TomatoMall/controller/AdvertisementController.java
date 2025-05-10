package cn.edu.nju.TomatoMall.controller;


import cn.edu.nju.TomatoMall.models.dto.advertisements.UpdateAdvertisementRequest;
import cn.edu.nju.TomatoMall.models.dto.advertisements.AdvertisementBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.advertisements.AddAdvertisementRequest;

import cn.edu.nju.TomatoMall.service.AdvertisementService;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    /**
     * 获取广告列表
     */
    @GetMapping("/all")
    public ApiResponse<Page<AdvertisementBriefResponse>> getAdvertisementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {

        Page<AdvertisementBriefResponse> advertisementPage = advertisementService.getAdvertisementList(page, size, field, order);
        return ApiResponse.success(advertisementPage);
    }

    /**
     * 创建新广告
     */
    @PostMapping(path = "/create", consumes = "multipart/form-data")
    public ApiResponse<Boolean> createAdvertisement(@ModelAttribute AddAdvertisementRequest params) {
        return ApiResponse.success(advertisementService.createAdvertisement(params));
    }



    /**
     * 更新广告信息
     */
    @PatchMapping(path = "/{advertisementId}", consumes = "multipart/form-data")
    public ApiResponse<Boolean> updateAdvertisement(@PathVariable int advertisementId, @ModelAttribute UpdateAdvertisementRequest params) {
        return ApiResponse.success(advertisementService.updateAdvertisement(advertisementId, params));
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{advertisementId}")
    public ApiResponse<String> deleteAdvertisement(@PathVariable int advertisementId) {
        return ApiResponse.success(advertisementService.deleteAdvertisement(advertisementId));
    }


}
