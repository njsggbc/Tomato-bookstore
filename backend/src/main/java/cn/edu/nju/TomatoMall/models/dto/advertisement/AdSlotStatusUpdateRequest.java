package cn.edu.nju.TomatoMall.models.dto.advertisement;

import lombok.Data;

import java.util.List;

@Data

public class AdSlotStatusUpdateRequest {
    private Integer spaceId;
    private List<Integer> slotIds;
    private Boolean available;
    private Boolean active;
}
