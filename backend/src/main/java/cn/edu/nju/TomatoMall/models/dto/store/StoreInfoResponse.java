package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Data;

@Data
public class StoreInfoResponse {
    int id;
    String name;
    String description;
    String address;
    String logoUrl;
    String regTime;
    String status;
    Integer score;

    public StoreInfoResponse(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.address = store.getAddress();
        this.logoUrl = store.getLogoUrl();
        this.regTime = store.getCreateTime().toString();
        this.status = store.getStatus().toString();
        this.score = store.getScore();
    }
}
