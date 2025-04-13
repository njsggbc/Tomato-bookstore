package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreInfoResponse {
    int id;
    String name;
    String description;
    String address;
    String logoUrl;
    String regTime;
    String status;
    int score;

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
