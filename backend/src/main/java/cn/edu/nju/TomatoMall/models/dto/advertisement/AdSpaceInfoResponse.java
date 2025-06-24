package cn.edu.nju.TomatoMall.models.dto.advertisement;

import cn.edu.nju.TomatoMall.models.po.AdvertisementSpace;
import lombok.Data;

@Data
public class AdSpaceInfoResponse {
    private Integer id;
    private String label;
    private String type;
    private Integer cycle;
    private Integer segment;

    public AdSpaceInfoResponse(AdvertisementSpace space) {
        this.id = space.getId();
        this.label = space.getLabel();
        this.type = space.getType().toString();
        this.cycle = space.getCycle();
        this.segment = space.getSegment();
    }
}