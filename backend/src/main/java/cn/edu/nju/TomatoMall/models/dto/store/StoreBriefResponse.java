package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreBriefResponse {
    int id;
    String name;
    String description;
    String logoUrl;
    String regTime;
    Integer score;

    public StoreBriefResponse(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.logoUrl = store.getLogoUrl();
        this.regTime = store.getCreateTime().toString();
        this.score = store.getScore();
    }
}
