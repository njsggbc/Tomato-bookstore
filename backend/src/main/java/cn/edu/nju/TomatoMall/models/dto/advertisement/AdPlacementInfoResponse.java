package cn.edu.nju.TomatoMall.models.dto.advertisement;

import cn.edu.nju.TomatoMall.enums.AdPlacementStatus;
import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdPlacementInfoResponse {
    private int id;
    private int adId;
    private int adSpaceId;
    private AdPlacementStatus status;
    private List<LocalDateTime> displayTimeList;
    private int displayDurationInHours;
    private LocalDateTime createTime;

    public AdPlacementInfoResponse(AdvertisementPlacement placement) {
        this.id = placement.getId();
        this.adId = placement.getAdvertisement().getId();
        this.adSpaceId = placement.getSpace().getId();
        this.status = placement.getStatus();
        this.displayTimeList = placement.getDisplayTimeList();
        this.displayDurationInHours = placement.getDisplayDuration();
        this.createTime = placement.getCreateTime();
    }
}
