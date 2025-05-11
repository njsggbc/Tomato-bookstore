package cn.edu.nju.TomatoMall.models.dto.advertisement;

import cn.edu.nju.TomatoMall.models.po.Advertisement;
import lombok.Data;

@Data
public class AdInfoResponse {
    private Integer id;
    private String title;
    private String content; // 广告图片URL
    private String linkUrl; // 跳转链接
    private String status;
    private Integer storeId;
    private String storeName;
    private String createTime;

    public AdInfoResponse(Advertisement ad) {
        this.id = ad.getId();
        this.title = ad.getTitle();
        this.content = ad.getContent();
        this.linkUrl = ad.getLinkUrl();
        this.status = ad.getStatus().toString();
        this.storeId = ad.getStore().getId();
        this.storeName = ad.getStore().getName();
        this.createTime = ad.getCreateTime().toString();
    }
}